package com.penglobe.server.controller;

import com.penglobe.server.dto.AuthDTO.*;
import com.penglobe.server.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "로그인/회원가입 API")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인", description = "RN앱에서 받은 accessToken을 서버로 전달하면, 서버가 카카오 /v2/user/me로 검증 후 JWT를 발급합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<AuthResponse> kakao(@Valid @RequestHeader KakaoLoginRequest req) {
        return ResponseEntity.ok(authService.loginWithKakao(req.accessToken));
    }

    @Operation(summary = "자체 회원가입", description = "이메일/비밀번호/닉네임/지역을 입력받아 계정을 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody LocalSignupRequest req) {
        authService.signupLocal(req);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자체 로그인", description = "이메일/비밀번호로 로그인 후 JWT를 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LocalLoginRequest req) {
        return ResponseEntity.ok(authService.loginLocal(req));
    }

    @Operation(summary = "카카오 사용자 프로필 완료", description = "카카오 로그인 사용자의 지역/닉네임 등을 입력 받아 isProfileComplete=true로 업데이트합니다.")
    @PatchMapping("/me/complete-profile")
    public ResponseEntity<?> completeProfile(
            @PathVariable long userId,
            @Valid @RequestBody CompleteProfileRequest req) {
        authService.completeProfile(userId, req);
        return ResponseEntity.ok().build();
    }

}
