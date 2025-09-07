package com.penglobe.server.controller;

import com.penglobe.server.dto.DietDTO;
import com.penglobe.server.service.DietService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
@Tag(name = "Diet", description = "식단 절감량 기록 API")
public class DietController {

    private final DietService dietService;

    @Operation(summary = "식단 기록 생성", description = "하루 최대 3회까지 등록 가능합니다.")
    @PostMapping("/{userId}")
    public DietDTO create(
            @PathVariable Long userId,
            @RequestParam BigDecimal co2Kg
    ) {
        return dietService.createDietRecordWithDailyLimit(userId, co2Kg);
    }

    @Operation(summary = "오늘 합계 조회", description = "해당 사용자의 오늘 하루 절감 CO₂ 총합을 반환합니다.")
    @GetMapping("/{userId}/today/sum")
    public BigDecimal getTodaySum(@PathVariable Long userId) {
        return dietService.getTodaySum(userId);
    }

    @Operation(summary = "특정 날짜 합계 조회", description = "달력에서 선택한 특정 날짜의 절감 CO₂ 총합을 반환합니다.")
    @GetMapping("/{userId}/date/sum")
    public BigDecimal getDailySumByDate(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return dietService.getDailySumByDate(userId, date);
    }

    @Operation(summary = "특정 기간 합계 조회", description = "특정 기간의 절감 CO₂ 총합을 반환합니다. [start, end) 범위")
    @GetMapping("/{userId}/sum")
    public BigDecimal getSumByPeriod(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return dietService.getSumByPeriod(userId, start, end);
    }
}
