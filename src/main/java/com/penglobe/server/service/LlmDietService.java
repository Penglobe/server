// src/main/java/com/penglobe/server/service/LlmDietService.java
package com.penglobe.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penglobe.server.dto.diet.DietRequestDTO;
import com.penglobe.server.dto.diet.DietResultDTO;
import com.penglobe.server.llm.GroqClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LlmDietService {

    private final GroqClient groqClient;
    private final ObjectMapper om = new ObjectMapper();

    // ===== 모드별 기본 상수 (예시값: 프로젝트 기준으로 조정) =====
    private static final BigDecimal COOK_GAS_PER_MIN_KG       = bd("0.00015"); // 집: 가스 1분당 kgCO2e
    private static final BigDecimal COOK_DEFAULT_MINUTES      = bd("10");

    private static final BigDecimal TRANS_MOTORBIKE_PER_KM    = bd("0.103");   // 배달: 오토바이 km당
    private static final BigDecimal TRANS_DELIVERY_DEFAULT_KM = bd("3.0");

    private static final BigDecimal TRANS_WALK_PER_KM         = bd("0.000");   // 포장: 도보 km당
    private static final BigDecimal TRANS_TAKEOUT_DEFAULT_KM  = bd("0.8");

    private static final BigDecimal PKG_PLASTIC_SET_KG        = bd("0.050");   // 배달: 플라스틱 세트 1
    private static final BigDecimal PKG_PAPER_SET_KG          = bd("0.020");   // 포장: 종이 세트 1

    // ===== 유틸 =====
    private static BigDecimal bd(String s) { return new BigDecimal(s); }
    /** 셋째 자리에서 반올림 → 둘째 자리까지 */
    private static BigDecimal r2(BigDecimal v) { return v.setScale(2, RoundingMode.HALF_UP); }

    private static BigDecimal readDecimal(JsonNode n) {
        if (n == null || n.isNull()) return BigDecimal.ZERO;
        // 숫자/문자 모두 안전 처리
        return new BigDecimal(n.asText());
    }

    // ===== LLM 프롬프트 (음식 '자체' 배출량만 계산) =====
    private String buildPrompt(List<DietRequestDTO.FoodItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items가 비어 있습니다.");
        }
        try {
            String itemsJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(items);
            return """
역할: 음식별 탄소배출량(‘음식 자체’만) 추정기.

목표:
- 아래 '입력 항목'을 보고 각 음식의 kgCO2e(음식자체)와 총합을 계산한다.
- ⚠️ 조리/운송/포장/잔반 등 추가 가감은 네가 계산하지 않는다(서버에서 합산한다).

규칙:
- 질량 totalSize(g)를 kg로 환산해 사용한다 (kg = g / 1000).
- 전형적인 범위에 근거해 추정하되, 모호하면 보수적으로 낮게 추정한다.
- 모든 수치는 소수 셋째 자리에서 반올림하여 소수 둘째 자리(HALF_UP)로 반환한다.
- 출력은 오직 JSON 한 덩어리만 허용(여분 텍스트 금지).

입력 항목(items):
%s

출력 스키마(반드시 엄수):
{
  "totalCo2Kg": number,
  "items": [ { "name": string, "co2Kg": number } ],
  "unknown": string[]
}
""".formatted(itemsJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ===== 응답에서 JSON만 추출 =====
    private JsonNode parseJsonOnly(String text) {
        var m = Pattern.compile("\\{[\\s\\S]*?\\}").matcher(text); // 비탐욕
        if (!m.find()) throw new IllegalArgumentException("LLM 응답이 JSON이 아님");
        String json = m.group();
        try { return om.readTree(json); }
        catch (Exception e) { throw new IllegalArgumentException("LLM JSON 파싱 실패: " + e.getMessage(), e); }
    }

    // ===== 공개 API =====
    public DietResultDTO calculate(DietRequestDTO req) {
        // A) 입력 검증
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("items가 비어 있습니다.");
        }

        // 1) LLM으로 음식 '자체' 배출량 계산
        String prompt = buildPrompt(req.getItems());
        String content = groqClient.chat(prompt);

        JsonNode root = parseJsonOnly(content);
        if (!root.has("totalCo2Kg") || !root.has("items") || !root.has("unknown")) {
            throw new IllegalArgumentException("LLM 응답에 필수 필드(totalCo2Kg/items/unknown)가 없습니다.");
        }

        BigDecimal llmTotal = r2(readDecimal(root.get("totalCo2Kg")));

        List<DietResultDTO.ItemResult> items = new ArrayList<>();
        for (JsonNode n : root.get("items")) {
            if (!n.has("name") || !n.has("co2Kg")) continue; // 불량 항목 스킵
            String name = n.get("name").asText();
            BigDecimal v = r2(readDecimal(n.get("co2Kg")));
            items.add(DietResultDTO.ItemResult.builder().name(name).co2Kg(v).build());
        }

        // 합계 검증/보정 (허용 오차 0.01)
        BigDecimal foodSum = items.stream()
                .map(DietResultDTO.ItemResult::getCo2Kg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (llmTotal.subtract(foodSum).abs().compareTo(bd("0.01")) > 0) {
            llmTotal = r2(foodSum);
        }

        // 2) eatMode 기본 가감 계산 (프론트는 eatMode만 보냄)
        Adjustment adj = computeAdjustments(req.getEatMode());

        // 3) 총합 = 음식자체 + 가감
        BigDecimal total = r2(llmTotal.add(adj.total()));

        return DietResultDTO.builder()
                .totalCo2Kg(total)
                .items(items)
                .unknown(readUnknown(root))
                // 필요 시 LCA 설명용 브레이크다운 필드가 있다면 아래를 DTO에 추가
                // .breakdown(adj.asMap())
                .build();
    }

    // ===== 내부: unknown 읽기 =====
    private List<String> readUnknown(JsonNode root) {
        List<String> unknown = new ArrayList<>();
        for (JsonNode n : root.get("unknown")) unknown.add(n.asText());
        return unknown;
    }

    // ===== 내부: eatMode 기본 가감 =====
    private Adjustment computeAdjustments(DietRequestDTO.EatMode mode) {
        if (mode == null) mode = DietRequestDTO.EatMode.RESTAURANT;

        BigDecimal cook = BigDecimal.ZERO;
        BigDecimal transport = BigDecimal.ZERO;
        BigDecimal packaging = BigDecimal.ZERO;
        BigDecimal waste = BigDecimal.ZERO;

        switch (mode) {
            case HOME -> {
                // 집: 가스 10분 조리
                cook = COOK_GAS_PER_MIN_KG.multiply(COOK_DEFAULT_MINUTES);
            }
            case DELIVERY -> {
                // 배달: 오토바이 3km + 플라스틱 세트 1
                transport = TRANS_MOTORBIKE_PER_KM.multiply(TRANS_DELIVERY_DEFAULT_KM);
                packaging = PKG_PLASTIC_SET_KG;
            }
            case TAKEOUT -> {
                // 포장: 도보 0.8km + 종이 세트 1
                transport = TRANS_WALK_PER_KM.multiply(TRANS_TAKEOUT_DEFAULT_KM);
                packaging = PKG_PAPER_SET_KG;
            }
            case RESTAURANT -> {
                // 식당: 잔반 기본 0
                waste = BigDecimal.ZERO;
            }
        }
        // 최종 반올림(둘째 자리)
        return new Adjustment(r2(cook), r2(transport), r2(packaging), r2(waste));
    }

    // ===== 가감 컨테이너 =====
    private record Adjustment(BigDecimal cook, BigDecimal transport, BigDecimal packaging, BigDecimal waste) {
        BigDecimal total() {
            return cook.add(transport).add(packaging).add(waste);
        }
        Map<String, BigDecimal> asMap() {
            return Map.of(
                    "cook", cook, "transport", transport,
                    "packaging", packaging, "waste", waste
            );
        }
    }
}
