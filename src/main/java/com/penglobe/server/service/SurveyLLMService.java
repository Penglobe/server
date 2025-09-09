package com.penglobe.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.penglobe.server.dto.survey.SurveyResultDTO;
import com.penglobe.server.dto.survey.TopCo2DTO;
import com.penglobe.server.llm.GroqClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SurveyLLMService {

    private final GroqClient groqClient; // LLM 호출 클라이언트
    private final ObjectMapper om = new ObjectMapper();

    // ---------------- LLM 피드백 생성 ----------------
    private String buildFeedbackPrompt(List<String> top3) {
        try {
            String itemsJson = om.writerWithDefaultPrettyPrinter().writeValueAsString(top3);
            return """
                역할: 설문조사 피드백 생성기.

                목표:
                - 사용자가 선택한 top3 항목은 재활용품, 일회용품, 종이타월 사용, 음식물쓰레기 절감, 불필요한 전자기기 절약 중 절감량이 높은 top3.
                - 사용자가 잘 한 건 칭찬하고 못 한건 충고해줘서
                - 사용자가 친환경 활동을 하라 수 있게 피드백. 
                - 2줄 출력
                
                - 출력은 반드시 JSON 한 덩어리만 반환하고 여분 텍스트 금지.

                입력 top3:
                %s

                출력 스키마:
                {
                  "feedback": string
                }
                """.formatted(itemsJson);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode parseJsonOnly(String text) {
        var m = Pattern.compile("\\{[\\s\\S]*\\}").matcher(text);
        if (!m.find()) throw new IllegalArgumentException("LLM 응답이 JSON이 아님");
        try {
            return om.readTree(m.group());
        } catch (Exception e) {
            throw new IllegalArgumentException("LLM JSON 파싱 실패: " + e.getMessage(), e);
        }
    }

    // ---------------- 결과 + 피드백 생성 ----------------
    public SurveyResultDTO generateFeedback(SurveyResultDTO surveyResult, List<TopCo2DTO> top3) {
        // top3 이름 리스트 추출
        List<String> top3Names = top3.stream()
                .map(TopCo2DTO::getCode)
                .collect(Collectors.toList());

        // LLM 호출
        String prompt = buildFeedbackPrompt(top3Names);
        String content = groqClient.chat(prompt);

        // JSON 파싱
        JsonNode root = parseJsonOnly(content);
        if (!root.has("feedback"))
            throw new IllegalArgumentException("LLM 응답에 feedback 필드 없음");

        // DTO에 feedback 추가
        surveyResult.setFeedback(root.get("feedback").asText());

        return surveyResult;
    }
}
