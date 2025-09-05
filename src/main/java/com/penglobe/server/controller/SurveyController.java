package com.penglobe.server.controller;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.survey.SurveyItemDTO;
import com.penglobe.server.dto.survey.SurveyResultDTO;
import com.penglobe.server.dto.survey.SurveySubmitRequestDTO;
import com.penglobe.server.service.SurveyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name="Survey API", description = "설문조회 및 제출 api")
@RestController
@RequestMapping("/surveys")
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService surveyService;

    //설문 가져오기
    @Operation(summary = "설문조회", description = "설문조사 항목과 선택지를 조회합니다.")
    @GetMapping("/today")
    public ResponseEntity<List<SurveyItemDTO>> getTodaySurvey() {
        List<SurveyItemDTO> todaySurvey = surveyService.getTodaySurvey();
        return ResponseEntity.ok(todaySurvey);
    }

    //설문 제출
    //총 Co2 계산 -> top3
    @Operation(summary = "설문 제출/Top3", description = "사용자가 설문을 제출하면 총 CO2와 Top3 항목을 계산합니다.")
    @PostMapping("/submit/{userId}")
    public ResponseEntity<?> submitSurvey(@RequestBody SurveySubmitRequestDTO dto) {
       SurveyResultDTO result = surveyService.submitSurvey(dto);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
