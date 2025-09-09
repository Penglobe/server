// src/main/java/.../service/LlmCarbonService.java
package com.penglobe.server.service;

import com.fasterxml.jackson.databind.*;
import com.penglobe.server.dto.diet.DietResultDTO;
import com.penglobe.server.dto.diet.DietRequestDTO;
import com.penglobe.server.llm.GroqClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.*;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LlmDietService {

    private final GroqClient groqClient;
    private final ObjectMapper om = new ObjectMapper();

    private static BigDecimal roundHalfUp(BigDecimal v, int scale) {
        return v == null ? null : v.setScale(scale, RoundingMode.HALF_UP);
    }

    private String buildPrompt(List<DietRequestDTO.FoodItem> items) {
        try {
            String itemsJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(items);
            return """
      역할: 음식별 탄소배출량 추정기.

      목표:
      - 아래 '입력 항목'을 보고 각 음식의 kgCO2e를 추정하고, 총합을 계산하라.
      - 배출계수표는 제공하지 않는다. 너의 일반적인 지식을 사용하되, 확실하지 않으면 추정하지 말고 unknown에 넣어라.

      규칙:
      - 각 항목의 질량 totalSize(g)를 kg로 환산해 사용한다 (kg = g / 1000).
      - 가능하면 음식 카테고리를 판단해 전형적인 배출량 범위에 근거해 추정하라.
      - 불확실하면 보수적(낮은) 추정을 하고, 아주 모호하면 unknown에 넣어라.
      - 모든 수치는 소수 셋째에서 반올림하여 소수 둘째 자리(HALF_UP)로 반환하라.
      - 출력은 오직 JSON 한 덩어리만 허용. 여분 텍스트 금지.

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

    private JsonNode parseJsonOnly(String text) {
        var m = Pattern.compile("\\{[\\s\\S]*\\}").matcher(text);
        if (!m.find()) throw new IllegalArgumentException("LLM 응답이 JSON이 아님");
        String json = m.group();
        try { return om.readTree(json); }
        catch (Exception e) { throw new IllegalArgumentException("LLM JSON 파싱 실패: " + e.getMessage(), e); }
    }

    public DietResultDTO calculate(DietRequestDTO req) {
        String prompt = buildPrompt(req.getItems());
        String content = groqClient.chat(prompt);

        JsonNode root = parseJsonOnly(content);
        if (!root.has("totalCo2Kg") || !root.has("items") || !root.has("unknown"))
            throw new IllegalArgumentException("필수 필드 누락");

        BigDecimal total = roundHalfUp(new BigDecimal(root.get("totalCo2Kg").asText()), 1);

        List<DietResultDTO.ItemResult> items = new ArrayList<>();
        for (JsonNode n : root.get("items")) {
            String name = n.get("name").asText();
            BigDecimal v = roundHalfUp(new BigDecimal(n.get("co2Kg").asText()), 1);
            items.add(DietResultDTO.ItemResult.builder().name(name).co2Kg(v).build());
        }

        List<String> unknown = new ArrayList<>();
        for (JsonNode n : root.get("unknown")) unknown.add(n.asText());

        // 총합 보정 (per-item 합과 0.1 이상 차이나면 교정)
        BigDecimal sum = items.stream()
                .map(DietResultDTO.ItemResult::getCo2Kg)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);
        if (total == null || total.subtract(sum).abs().compareTo(new BigDecimal("0.1")) > 0) total = sum;

        return DietResultDTO.builder()
                .totalCo2Kg(total)
                .items(items)
                .unknown(unknown)
                .build();
    }
}
