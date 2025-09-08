// src/main/java/com/penglobe/server/controller/UserController.java
package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.UserProfileDTO;
import com.penglobe.server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 프로필/누적 지표 조회", description = "User + UserCounters + totalScore(합계) 반환")
    public ApiResponse<UserProfileDTO> me(Authentication authentication) {
        // MissionController에서와 동일하게 principal = userId 로 가정
        Long userId = (authentication == null) ? null : (Long) authentication.getPrincipal();
        if (userId == null) {
            throw new IllegalArgumentException("인증 정보가 없습니다.");
        }
        UserProfileDTO dto = userService.getMe(userId);
        return ApiResponse.success(HttpStatus.OK.value(), "me 조회 성공", dto);
    }
}
