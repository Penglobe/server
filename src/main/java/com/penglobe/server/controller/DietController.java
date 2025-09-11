package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.diet.DietDTO;
import com.penglobe.server.dto.diet.DietRequestDTO;
import com.penglobe.server.dto.diet.DietResultDTO;
import com.penglobe.server.service.DietService;
import com.penglobe.server.service.LlmDietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
@Tag(name = "Diet", description = "식단 절감량 기록 API")
public class DietController {

    private final DietService dietService;
    private final LlmDietService llmDietService;

    @Operation(summary = "식단 기록 생성", description = "하루 최대 3회까지 등록 가능합니다.")
    @PostMapping(value = "/ingest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DietRequestDTO ingest(@RequestBody DietRequestDTO req) {
        log.info("📩 식단 요청 도착: userId={}, items={}", req.getUserId(), req.getItems());
        return req;
    }

    @Operation(summary = "식단 기록 & LLM 연결", description = "식단 탄소 배출량을 AI가 계산하여 반환합니다.")
    @PostMapping(value = "/ingest/calc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<DietResultDTO> calc(@RequestBody DietRequestDTO req) {
        return ApiResponse.success(llmDietService.calculate(req));
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DietSaveRequest {
        private BigDecimal co2Kg;
    }

    @Operation(summary = "식단 절감량 저장", description = "절약한 CO₂를 저장합니다. (하루 최대 3회)")
    @PostMapping(value = "/ingest/save", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DietDTO> create(@RequestBody @Validated DietSaveRequest body, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        var dto = dietService.createDietRecordWithDailyLimit(userId, body.getCo2Kg());
        return ApiResponse.success(dto);
    }

    // 오늘 등록 횟수 조회
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TodayCountDTO {
        private int todayCount;
    }

    @Operation(summary = "오늘 등록 횟수 조회", description = "해당 사용자의 오늘 식단 기록 개수를 반환합니다.")
    @GetMapping(value = "/{userId}/today/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<TodayCountDTO> getTodayCount(@PathVariable Long userId) {
        int count = dietService.getTodayCount(userId);
        return ApiResponse.success(TodayCountDTO.builder().todayCount(count).build());
    }

    @Operation(summary = "오늘 합계 조회", description = "해당 사용자의 오늘 하루 절감 CO₂ 총합을 반환합니다.")
    @GetMapping(value = "/{userId}/today/sum", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BigDecimal> getTodaySum(@PathVariable Long userId) {
        BigDecimal sum = dietService.getTodaySum(userId);
        return ApiResponse.success(sum);
    }

    @Operation(summary = "특정 날짜 합계 조회", description = "달력에서 선택한 특정 날짜의 절감 CO₂ 총합을 반환합니다.")
    @GetMapping(value = "/{userId}/date/sum", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BigDecimal> getDailySumByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        BigDecimal sum = dietService.getDailySumByDate(userId, date);
        return ApiResponse.success(sum);
    }

    @Operation(summary = "특정 기간 합계 조회", description = "특정 기간의 절감 CO₂ 총합을 반환합니다. [start, end) 범위")
    @GetMapping(value = "/{userId}/sum", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<BigDecimal> getSumByPeriod(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        BigDecimal sum = dietService.getSumByPeriod(userId, start, end);
        return ApiResponse.success(sum);
    }
}
