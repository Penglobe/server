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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

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
        Long userId = (Long) authentication.getPrincipal(); // Placeholder: Adjust based on actual UserDetails implementation

        DailyCarbonReductionDTO dailyReduction = myPageService.getDailyCarbonReduction(userId, date);
        return ResponseEntity.ok(ApiResponse.success(dailyReduction));
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
