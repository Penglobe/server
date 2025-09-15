package com.penglobe.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penglobe.server.dto.diet.DietRequestDTO;
import com.penglobe.server.dto.diet.DietResultDTO;
import com.penglobe.server.llm.GroqClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmDietService {

    private final GroqClient groqClient;
    private final ObjectMapper om = new ObjectMapper();

    // ===== 로깅 설정 =====
    private static final int LOG_TRUNCATE = 800;         // 긴 문자열 truncate 길이
    private static final boolean LOG_PROMPT = false;     // 프롬프트 전체 로그 노출 (개발시에만 true 권장)
    private static final boolean LOG_JSON_RAW = false;   // LLM raw 응답 전체 로그 (개발시에만 true 권장)

    // ===== 모드별 기본 상수 (예시값: 프로젝트 기준으로 조정) =====
    private static final BigDecimal COOK_GAS_PER_MIN_KG       = bd("0.00015"); // 집: 가스 1분당 kgCO2e
    private static final BigDecimal COOK_DEFAULT_MINUTES      = bd("10");

    private static final BigDecimal TRANS_MOTORBIKE_PER_KM    = bd("0.103");   // 배달: 오토바이 km당
    private static final BigDecimal TRANS_DELIVERY_DEFAULT_KM = bd("3.0");

    private static final BigDecimal TRANS_WALK_PER_KM         = bd("0.000");   // 포장: 도보 km당
    private static final BigDecimal TRANS_TAKEOUT_DEFAULT_KM  = bd("0.8");

    private static final BigDecimal PKG_PLASTIC_SET_KG        = bd("0.050");   // 배달: 플라스틱 세트 1
    private static final BigDecimal PKG_PAPER_SET_KG          = bd("0.020");   // 포장: 종이 세트 1

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
    /** 셋째 자리에서 반올림 → 둘째 자리까지 */
    private static BigDecimal r2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private static BigDecimal readDecimal(JsonNode n) {
        if (n == null || n.isNull()) return BigDecimal.ZERO;
        return new BigDecimal(n.asText());
    }

    private static String truncate(String s) {
        if (s == null) return "null";
        return s.length() > LOG_TRUNCATE ? s.substring(0, LOG_TRUNCATE) + "\n…(truncated)" : s;
    }

    // ===== LLM 프롬프트 (음식 '자체' 배출량만 계산) =====
    private String buildPrompt(List<DietRequestDTO.FoodItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items가 비어 있습니다.");
        }
        try {
            String itemsJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(items);
            String prompt = """
역할: 음식별 탄소배출량(‘음식 자체’만) 추정기.

목표:
- 아래 '입력 항목'을 보고 각 음식의 kgCO2e(음식자체)와 총합을 계산한다.
- ⚠️ 조리/운송/포장/잔반 등 추가 가감은 네가 계산하지 않는다(서버에서 합산한다).

규칙:
- 질량 totalSize(g)를 kg로 환산해 사용한다 (kg = g / 1000).
- 전형적인 범위에 근거해 추정하되, 모호하면 보수적으로 낮게 추정한다.
- 모든 수치는 소수 셋째 자리에서 반올림하여 소수 둘째 자리(HALF_UP)로 반환한다.
- 출력은 오직 JSON 한 덩어리만 허용(여분 텍스트/코드펜스 금지).

입력 항목(items):
%s

출력 스키마(반드시 엄수):
{
  "totalCo2Kg": number,
  "items": [ { "name": string, "co2Kg": number } ],
  "unknown": string[]
}
""".formatted(itemsJson);

            if (LOG_PROMPT && log.isDebugEnabled()) {
                log.debug("[Diet][PROMPT]\n{}", truncate(prompt));
            } else {
                // 민감도 낮은 요약 로그
                log.debug("[Diet][PROMPT] items={} names={}",
                        items.size(),
                        items.stream().map(i -> i.getName() == null ? "(null)" : i.getName()).toList());
            }
            return prompt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ===== JSON 파싱 (튼튼한 버전) =====
    private JsonNode parseJsonRobust(String content) {
        if (content == null) throw new IllegalArgumentException("LLM 응답이 비었습니다.");

        if (LOG_JSON_RAW && log.isDebugEnabled()) {
            log.debug("[Diet][LLM RAW]\n{}", truncate(content));
        } else {
            log.debug("[Diet][LLM head] {}", content.substring(0, Math.min(300, content.length())));
        }

        // 0) 코드펜스/노이즈 제거
        String cleaned = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // try1: 전체 파싱
        try { return om.readTree(cleaned); } catch (Exception ignore) {}

        // try2: 첫 '{' ~ 마지막 '}' 서브스트링
        int s = cleaned.indexOf('{'), e = cleaned.lastIndexOf('}');
        if (s >= 0 && e > s) {
            String sub = cleaned.substring(s, e + 1);
            try { return om.readTree(sub); } catch (Exception ignore) {
                // try3: 괄호/브래킷 보정
                String fixed = fixBrackets(sub);
                try { return om.readTree(fixed); } catch (Exception ignore2) {}
            }
        }

        // try4: 모델에 “JSON만” 재요청 (1회)
        String repaired = groqClient.chat("""
            너의 이전 출력이 유효한 JSON이 아니었어.
            아래 텍스트를 스키마에 맞는 **정확한 JSON**으로 고쳐서 **JSON만** 출력해.
            스키마:
            {
              "totalCo2Kg": number,
              "items": [ { "name": string, "co2Kg": number } ],
              "unknown": string[]
            }
            --- 원본 ---
            """ + content);
        return parseJsonOnly(repaired); // 얇은 파서 재사용
    }

    // 괄호/브래킷 닫힘 보정
    private String fixBrackets(String json) {
        int braces = 0, brackets = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : json.toCharArray()) {
            if (c == '{') braces++;
            if (c == '}') braces--;
            if (c == '[') brackets++;
            if (c == ']') brackets--;
            sb.append(c);
        }
        while (brackets > 0) { sb.append(']'); brackets--; }
        while (braces > 0) { sb.append('}'); braces--; }
        return sb.toString();
    }

    // (fallback) 얇은 파서 - 그대로 유지
    private JsonNode parseJsonOnly(String text) {
        var m = Pattern.compile("\\{[\\s\\S]*?\\}").matcher(text);
        if (!m.find()) throw new IllegalArgumentException("LLM 응답이 JSON이 아님");
        String json = m.group();
        try { return om.readTree(json); }
        catch (Exception e) { throw new IllegalArgumentException("LLM JSON 파싱 실패: " + e.getMessage(), e); }
    }

    // ===== 공개 API =====
    public DietResultDTO calculate(DietRequestDTO req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("items가 비어 있습니다.");
        }

        // 1) 프롬프트 생성 & LLM 호출
        String prompt = buildPrompt(req.getItems());
        String content = groqClient.chat(prompt);

        // 2) 파싱
        JsonNode root = parseJsonRobust(content);

        if (!root.has("totalCo2Kg") || !root.has("items") || !root.has("unknown")) {
            throw new IllegalArgumentException("LLM 응답에 필수 필드(totalCo2Kg/items/unknown)가 없습니다.");
        }

        // 3) 아이템/합계 파싱
        BigDecimal llmTotal = r2(readDecimal(root.get("totalCo2Kg")));

        List<DietResultDTO.ItemResult> items = new ArrayList<>();
        for (JsonNode n : root.get("items")) {
            if (!n.has("name") || !n.has("co2Kg")) continue;
            items.add(DietResultDTO.ItemResult.builder()
                    .name(n.get("name").asText())
                    .co2Kg(r2(readDecimal(n.get("co2Kg"))))
                    .build());
        }

        BigDecimal foodSum = items.stream()
                .map(DietResultDTO.ItemResult::getCo2Kg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 합계 보정 (오차 > 0.01이면 보정)
        boolean adjusted = false;
        if (llmTotal.subtract(foodSum).abs().compareTo(bd("0.01")) > 0) {
            adjusted = true;
            llmTotal = foodSum;
        }

        // 로그: LLM 결과와 항목 합계 비교
        log.info("[Diet][LLM] totalCo2Kg={} itemsSum={} adjusted={}", llmTotal, foodSum, adjusted);
        if (log.isDebugEnabled()) {
            log.debug("[Diet][LLM items] {}", items.stream().map(i -> i.getName() + "=" + i.getCo2Kg()).toList());
            log.debug("[Diet][LLM unknown] {}", readUnknown(root));
        }

        // 4) eatMode 가감 계산
        Adjustment adj = computeAdjustments(req.getEatMode());
        Map<String, BigDecimal> adjMap = adj.asMap();
        BigDecimal adjTotal = adj.total();

        log.info("[Diet][ADJ] mode={} cook={} transport={} packaging={} waste={} adjTotal={}",
                req.getEatMode(),
                adjMap.get("cook"), adjMap.get("transport"), adjMap.get("packaging"), adjMap.get("waste"),
                adjTotal
        );

        // 5) 최종 합계
        BigDecimal total = r2(llmTotal.add(adjTotal));
        log.info("[Diet][OUT] finalTotal={} (= food:{} + adj:{})", total, llmTotal, adjTotal);

        return DietResultDTO.builder()
                .totalCo2Kg(total)
                .items(items)
                .unknown(readUnknown(root))
                .build();
    }

    private List<String> readUnknown(JsonNode root) {
        List<String> unknown = new ArrayList<>();
        for (JsonNode n : root.get("unknown")) unknown.add(n.asText());
        return unknown;
    }

    private Adjustment computeAdjustments(DietRequestDTO.EatMode mode) {
        if (mode == null) mode = DietRequestDTO.EatMode.RESTAURANT;

        BigDecimal cook = BigDecimal.ZERO;
        BigDecimal transport = BigDecimal.ZERO;
        BigDecimal packaging = BigDecimal.ZERO;
        BigDecimal waste = BigDecimal.ZERO;

        switch (mode) {
            case HOME -> cook = COOK_GAS_PER_MIN_KG.multiply(COOK_DEFAULT_MINUTES);
            case DELIVERY -> {
                transport = TRANS_MOTORBIKE_PER_KM.multiply(TRANS_DELIVERY_DEFAULT_KM);
                packaging = PKG_PLASTIC_SET_KG;
            }
            case TAKEOUT -> {
                transport = TRANS_WALK_PER_KM.multiply(TRANS_TAKEOUT_DEFAULT_KM);
                packaging = PKG_PAPER_SET_KG;
            }
            case RESTAURANT -> waste = BigDecimal.ZERO;
        }
        return new Adjustment(r2(cook), r2(transport), r2(packaging), r2(waste));
    }

    private record Adjustment(BigDecimal cook, BigDecimal transport, BigDecimal packaging, BigDecimal waste) {
        BigDecimal total() { return cook.add(transport).add(packaging).add(waste); }
        Map<String, BigDecimal> asMap() {
            return Map.of("cook", cook, "transport", transport, "packaging", packaging, "waste", waste);
        }
    }
}
