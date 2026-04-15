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
    private static final boolean LOG_PROMPT = false;
    private static final boolean LOG_JSON_RAW = false;

    // ===== 모드별 기본 상수 (모두 '한 끼당' 기준) =====
    // 집(가정 조리) — 한 끼당 고정 배출량
    private static final BigDecimal COOK_HOME_PER_MEAL_KG = bd("0.00119"); // 예시값

    // 배달(오토바이 + 일회용기) — 배달 이동 + 포장
    private static final BigDecimal TRANS_MOTORBIKE_PER_KM    = bd("0.137"); // km당
    private static final BigDecimal TRANS_DELIVERY_DEFAULT_KM = bd("4.0");   // 한 끼당 평균 이동거리
    private static final BigDecimal PKG_PLASTIC_SET_KG        = bd("0.050"); // 한 끼당 포장세트

    // 테이크아웃 — 포장만
    private static final BigDecimal TAKEOUT_PACKAGING_PER_MEAL_KG = bd("0.050");

    // 식당 — 시설·운영 한 끼당 고정 배출량
    private static final BigDecimal RESTAURANT_FACILITY_PER_MEAL_KG = bd("0.00343"); // 예시값

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
    private static BigDecimal r2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private static BigDecimal readDecimal(JsonNode n) {
        if (n == null || n.isNull()) return BigDecimal.ZERO;
        return new BigDecimal(n.asText());
    }

    private static String truncate(String s) {
        if (s == null) return "null";
        return s.length() > LOG_TRUNCATE ? s.substring(0, LOG_TRUNCATE) + "\n…(truncated)" : s;
    }

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
- ⚠️ 조리/운송/포장/잔반/시설운영 등 추가 가감은 네가 계산하지 않는다(서버에서 합산한다).

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
                log.debug("[Diet][PROMPT] items={} names={}",
                        items.size(),
                        items.stream().map(i -> i.getName() == null ? "(null)" : i.getName()).toList());
            }
            return prompt;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode parseJsonRobust(String content) {
        if (content == null) throw new IllegalArgumentException("LLM 응답이 비었습니다.");

        if (LOG_JSON_RAW && log.isDebugEnabled()) {
            log.debug("[Diet][LLM RAW]\n{}", truncate(content));
        } else {
            log.debug("[Diet][LLM head] {}", content.substring(0, Math.min(300, content.length())));
        }

        String cleaned = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // 전체 파싱
        try { return om.readTree(cleaned); } catch (Exception ignore) {}

        int s = cleaned.indexOf('{'), e = cleaned.lastIndexOf('}');
        if (s >= 0 && e > s) {
            String sub = cleaned.substring(s, e + 1);
            try { return om.readTree(sub); } catch (Exception ignore) {
                // try3: 괄호/브래킷 보정
                String fixed = fixBrackets(sub);
                try { return om.readTree(fixed); } catch (Exception ignore2) {}
            }
        }

        // 모델에 “JSON만” 재요청
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
        return parseJsonOnly(repaired);
    }

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

    private JsonNode parseJsonOnly(String text) {
        var m = Pattern.compile("\\{[\\s\\S]*?\\}").matcher(text);
        if (!m.find()) throw new IllegalArgumentException("LLM 응답이 JSON이 아님");
        String json = m.group();
        try { return om.readTree(json); }
        catch (Exception e) { throw new IllegalArgumentException("LLM JSON 파싱 실패: " + e.getMessage(), e); }
    }

    public DietResultDTO calculate(DietRequestDTO req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("items가 비어 있습니다.");
        }

        // 1) 프롬프트 생성 & LLM 호출 (음식 자체 배출량만)
        String prompt = buildPrompt(req.getItems());
        String content = groqClient.chat(prompt);

        // 2) 파싱
        JsonNode root = parseJsonRobust(content);

        if (!root.has("totalCo2Kg") || !root.has("items") || !root.has("unknown")) {
            throw new IllegalArgumentException("LLM 응답에 필수 필드(totalCo2Kg/items/unknown)가 없습니다.");
        }

        // 3) 아이템/합계 파싱 + 이상치 검증
        BigDecimal llmTotalRaw = readDecimal(root.get("totalCo2Kg"));

        List<DietResultDTO.ItemResult> itemsRoundedForDto = new ArrayList<>();
        List<BigDecimal> itemCo2RawList = new ArrayList<>();
        int itemIndex = 0;
        for (JsonNode n : root.get("items")) {
            if (!n.has("name") || !n.has("co2Kg")) continue;
            String name = n.get("name").asText();
            BigDecimal co2Raw = readDecimal(n.get("co2Kg")); // raw 유지
            
            // 아이템별 이상치 검증 (음식양 기반)
            DietRequestDTO.FoodItem origItem = itemIndex < req.getItems().size() ? req.getItems().get(itemIndex) : null;
            BigDecimal validatedCo2 = validateAndClipItem(name, co2Raw, origItem);
            itemIndex++;
            
            itemCo2RawList.add(validatedCo2);

            itemsRoundedForDto.add(DietResultDTO.ItemResult.builder()
                    .name(name)
                    .co2Kg(r2(validatedCo2))
                    .build());
        }

        BigDecimal foodSumRaw = itemCo2RawList.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 합계 보정 (오차 > 0.01이면 LLM total을 아이템 합계로 교체)
        boolean adjusted = false;
        if (llmTotalRaw.subtract(foodSumRaw).abs().compareTo(bd("0.01")) > 0) {
            adjusted = true;
            llmTotalRaw = foodSumRaw;
        }

        log.info("[Diet][LLM] totalCo2Kg(raw)={} itemsSum(raw)={} adjusted={}", llmTotalRaw, foodSumRaw, adjusted);

        // 4) eatMode 가감 계산
        Adjustment adj = computeAdjustments(req.getEatMode());
        BigDecimal adjTotalRaw = adj.total();

        log.info("[Diet][ADJ raw] mode={} cook={} transport={} packaging={} facility={} waste={} adjTotal={}",
                req.getEatMode(),
                adj.cook, adj.transport, adj.packaging, adj.facility, adj.waste, adjTotalRaw
        );

        // 5) Total 이상치 검증
        BigDecimal finalTotalBeforeClip = llmTotalRaw.add(adjTotalRaw);
        BigDecimal finalTotalClipped = validateAndClipTotal(finalTotalBeforeClip);
        BigDecimal finalTotalRounded = r2(finalTotalClipped);

        log.info("[Diet][OUT] finalTotal={} (= food(raw):{} + adj(raw):{} → clipped:{})",
                finalTotalRounded, llmTotalRaw, adjTotalRaw, finalTotalClipped);

        // 6) 최종 DTO 반환
        return DietResultDTO.builder()
                .totalCo2Kg(finalTotalRounded)
                .items(itemsRoundedForDto)
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
        BigDecimal facility = BigDecimal.ZERO;
        BigDecimal waste = BigDecimal.ZERO;

        switch (mode) {
            case HOME -> {
                cook = COOK_HOME_PER_MEAL_KG;
            }
            case DELIVERY -> {
                transport = TRANS_MOTORBIKE_PER_KM.multiply(TRANS_DELIVERY_DEFAULT_KM);
                packaging = PKG_PLASTIC_SET_KG;
            }
            case TAKEOUT -> {
                packaging = TAKEOUT_PACKAGING_PER_MEAL_KG;
            }
            case RESTAURANT -> {
                facility = RESTAURANT_FACILITY_PER_MEAL_KG;
            }
        }
        return new Adjustment(cook, transport, packaging, facility, waste);
    }

    // ===== 이상치 보정 상수 =====
    private static final BigDecimal ITEM_MAX_CO2_KG = bd("3.0");           // 아이템 최대값
    private static final BigDecimal TOTAL_MIN_CO2_KG = bd("0.05");          // Total 최소값
    private static final BigDecimal TOTAL_MAX_CO2_KG = bd("5.0");           // Total 최대값
    private static final BigDecimal MAX_DENSITY_KG_PER_G = bd("0.02");     // 100g당 2kg 이상은 비정상

    /**
     * 아이템별 이상치 검증 및 클리핑
     * 1) 음수 → 0
     * 2) 음식양 기반 밀도 검증 (g당 최대 0.02kg)
     * 3) 최대값 클리핑 (3.0kg)
     */
    private BigDecimal validateAndClipItem(String foodName, BigDecimal co2Kg, DietRequestDTO.FoodItem origItem) {
        // 음수 → 0
        if (co2Kg.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("[Diet][CLIP-ITEM] {} 음수값: {}kg → 0kg", foodName, co2Kg);
            return BigDecimal.ZERO;
        }

        // 음식양 기반 밀도 검증
        if (origItem != null) {
            BigDecimal servingGrams = extractServingGrams(origItem);
            if (servingGrams != null && servingGrams.compareTo(BigDecimal.ZERO) > 0) {
                // 밀도 = co2Kg / (servingGrams / 1000)
                BigDecimal servingKg = servingGrams.divide(bd("1000"), 6, RoundingMode.HALF_UP);
                BigDecimal density = co2Kg.divide(servingKg, 6, RoundingMode.HALF_UP);
                if (density.compareTo(MAX_DENSITY_KG_PER_G) > 0) {
                    BigDecimal clipped = servingKg.multiply(MAX_DENSITY_KG_PER_G);
                    log.warn("[Diet][CLIP-ITEM] {} 비정상 밀도: {:.4f}kg/g ({} g) → {}kg",
                            foodName, density, servingGrams, clipped);
                    return clipped;
                }
            }
        }

        // 최대값 클리핑
        if (co2Kg.compareTo(ITEM_MAX_CO2_KG) > 0) {
            log.warn("[Diet][CLIP-ITEM] {} 최대값 초과: {}kg → {}kg", foodName, co2Kg, ITEM_MAX_CO2_KG);
            return ITEM_MAX_CO2_KG;
        }

        return co2Kg;
    }

    /**
     * 음식양 추출 (serving 또는 amount에서)
     * "100g" → 100, "2개" → null
     */
    private BigDecimal extractServingGrams(DietRequestDTO.FoodItem item) {
        if (item == null) return null;
        if (item.getServing() != null) {
            BigDecimal num = extractNumberFromString(item.getServing().toString());
            if (num != null) return num;
        }
        if (item.getAmount() != null) {
            BigDecimal num = extractNumberFromString(item.getAmount().toString());
            if (num != null) return num;
        }
        return null;
    }

    /**
     * 문자열에서 숫자 추출 (g 단위만)
     * "100g" → 100, "50.5g" → 50.5, "2개" → null
     */
    private BigDecimal extractNumberFromString(String str) {
        if (str == null) return null;
        var m = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*[gG]").matcher(str);
        if (m.find()) {
            try {
                return new BigDecimal(m.group(1));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Total 이상치 검증 및 클리핑
     * 범위: 0.05kg ~ 5.0kg
     */
    private BigDecimal validateAndClipTotal(BigDecimal totalCo2Kg) {
        // 최소값
        if (totalCo2Kg.compareTo(TOTAL_MIN_CO2_KG) < 0) {
            log.warn("[Diet][CLIP-TOTAL] 최소값 미만: {}kg → {}kg", totalCo2Kg, TOTAL_MIN_CO2_KG);
            return TOTAL_MIN_CO2_KG;
        }

        // 최대값
        if (totalCo2Kg.compareTo(TOTAL_MAX_CO2_KG) > 0) {
            log.warn("[Diet][CLIP-TOTAL] 최대값 초과: {}kg → {}kg", totalCo2Kg, TOTAL_MAX_CO2_KG);
            return TOTAL_MAX_CO2_KG;
        }

        return totalCo2Kg;
    }

    /** 조정값 컨테이너 — 내부는 반올림 없이 유지 */
    private record Adjustment(
            BigDecimal cook,
            BigDecimal transport,
            BigDecimal packaging,
            BigDecimal facility,
            BigDecimal waste
    ) {
        BigDecimal total() { return cook.add(transport).add(packaging).add(facility).add(waste); }
        Map<String, BigDecimal> asMap() {
            return Map.of(
                    "cook", cook,
                    "transport", transport,
                    "packaging", packaging,
                    "facility", facility,
                    "waste", waste
            );
        }
    }
}
