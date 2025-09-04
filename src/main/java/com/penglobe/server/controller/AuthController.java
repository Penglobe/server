package com.penglobe.server.controller;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.AuthDTO.*;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.security.JwtTokenProvider;
import com.penglobe.server.service.AuthService;
import com.penglobe.server.service.TokenService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그인/회원가입 API")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Operation(summary = "카카오 로그인", description = "RN앱에서 받은 accessToken을 서버로 전달하면, 서버가 카카오 /v2/user/me로 검증 후 JWT를 발급합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<AuthResponse>> kakao(@Valid @RequestBody KakaoLoginRequest request) {
        AuthResponse res = authService.loginWithKakao(request.getAccessToken());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, "카카오 로그인 성공", res));
    }

    @Operation(summary = "자체 회원가입", description = "이메일/비밀번호/닉네임/지역 입력으로 계정을 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody LocalSignupRequest req) {
        authService.signupLocal(req);
        return ResponseEntity
                .status(HttpStatus.CREATED) // 201 Created
                .body(ApiResponse.success(201, "회원가입 완료", null));
    }

    @Operation(summary = "자체 로그인", description = "이메일/비밀번호로 로그인 후 JWT 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LocalLoginRequest req) {
        AuthResponse res = authService.loginLocal(req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, "로그인 성공", res));
    }

    @Operation(
            summary = "액세스 토큰 재발급",
            description = "헤더 **X-Refresh-Token** 으로 리프레시 토큰을 보내면 " +
                    "**accessToken / refreshToken(로테이션)** 을 재발급합니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        Long userId = tokenService.validateAndGetUserId(refreshToken);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(401, "리프레시 토큰 만료 또는 유효하지 않음"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        String newAT = jwtTokenProvider.createToken(user.getUserId(), "USER");
        String newRT = tokenService.issueFor(user.getUserId()); // RT 로테이션

        return ResponseEntity.ok(
                ApiResponse.success(200, "재발급 성공",
                        Map.of("accessToken", newAT, "refreshToken", newRT))
        );
    }

    @Operation(
            summary = "로그아웃",
            description = "헤더 **X-Refresh-Token** 을 기준으로 해당 유저의 모든 리프레시 토큰을 폐기합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("X-Refresh-Token") String refreshToken
    ) {
        Long userId = tokenService.validateAndGetUserId(refreshToken);
        if (userId != null) {
            tokenService.revokeAllByUserId(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(200, "로그아웃 완료", null));
    }

    @Operation(summary = "카카오 사용자 프로필 완료",
            description = "카카오 로그인 사용자의 지역/닉네임 등을 저장하고 isProfileComplete=true로 업데이트")
    @PatchMapping("/me/{userId}/complete-profile")
    public ResponseEntity<ApiResponse<Void>> completeProfile(
            @PathVariable long userId,
            @Valid @RequestBody CompleteProfileRequest req) {
        authService.completeProfile(userId, req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(200, "프로필 완료", null));
    }

}
