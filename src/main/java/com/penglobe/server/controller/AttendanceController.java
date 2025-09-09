package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.service.AttendanceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "출석(랜덤 보상) API")
public class AttendanceController {

    private final AttendanceLogService attendanceLogService;

    @GetMapping("/popup")
    @Operation(
            summary = "오늘 모달 노출 여부",
            description = "attendance_logs에 오늘 레코드가 있고 shown_at이 비어 있으면 show=true를 반환합니다."
    )
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> popup(Authentication authentication) {
        Long userId = (authentication == null) ? null : (Long) authentication.getPrincipal();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(401, "로그인이 필요합니다."));
        }

        boolean show = attendanceLogService.shouldShowPopup(userId);
        return ResponseEntity.ok(
                ApiResponse.success(200, "OK", Map.of("show", show))
        );
    }

    @Operation(summary = "출석 보상 미리보기(지급 없음)")
    @GetMapping("/preview")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> preview(Authentication auth) {
        Long userId = (auth == null) ? null : (Long) auth.getPrincipal();
        int points = attendanceLogService.previewAttendanceReward(userId); // 지급 X
        return ResponseEntity.ok(
                ApiResponse.success(200, "출석 보상 미리보기", Map.of("rewardPoints", points))
        );
    }

    @PostMapping("/claim")
    @Operation(
            summary = "출석 보상 지급(받기)",
            description = "points_ledger에 reason=ATTENDANCE로 적립하고 attendance_logs.shown_at을 오늘로 갱신합니다. 하루 1회 멱등."
    )
    public ResponseEntity<ApiResponse<Map<String, Integer>>> claim(Authentication authentication) {
        Long userId = (authentication == null) ? null : (Long) authentication.getPrincipal();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(401, "로그인이 필요합니다."));
        }

        try {
            int reward = attendanceLogService.claimAttendanceReward(userId);
            return ResponseEntity.ok(
                    ApiResponse.success(200, "출석 보상 지급 완료",
                            Map.of("rewardPoints", reward))
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, e.getMessage()));
        }
    }
}
