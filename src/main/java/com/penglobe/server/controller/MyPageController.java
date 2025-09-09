package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageDTO>> getMyPageInfo(Authentication authentication) {
        Long userId = requireUserId(authentication);

        MyPageDTO myPageInfo = myPageService.getMyPageInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(myPageInfo));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<DailyCarbonReductionDTO>> getDailyCarbonReduction(
            Authentication authentication,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long userId = requireUserId(authentication);

        DailyCarbonReductionDTO dailyReduction = myPageService.getDailyCarbonReduction(userId, date);
        return ResponseEntity.ok(ApiResponse.success(dailyReduction));
    }

    @PostMapping("/add-dummy-data")
    public ResponseEntity<ApiResponse<Void>> addDummyData(Authentication authentication) {
        Long userId = requireUserId(authentication);
        myPageService.addDummyData(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "더미 데이터 추가 성공", null));
    }

    @GetMapping("/attendance-dates")
    public ResponseEntity<ApiResponse<List<String>>> getAttendanceDates(Authentication authentication) {
        Long userId = requireUserId(authentication);
        List<String> dates = myPageService.getAttendanceDates(userId);
        return ResponseEntity.ok(ApiResponse.success(dates));
    }

    @PostMapping("/reset-attendance")
    public ResponseEntity<ApiResponse<Void>> resetAttendance(Authentication authentication) {
        Long userId = requireUserId(authentication);
        myPageService.resetUserAttendanceCounters(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "출석 카운터 및 관련 활동 재설정 성공", null));
    }

    // ── 공통: Authentication에서 userId(Long) 안전하게 뽑기 ──
    private Long requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        Object p = authentication.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 인증 주체 형식");
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 주체가 유효하지 않습니다.");
    }
}
