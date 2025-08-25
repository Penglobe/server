package com.penglobe.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDTO {

    // RN 앱이 accessToken을 서버로 넘김
    public static class KakaoLoginRequest {
        @NotBlank
        public String accessToken;
    }

    // 공통 로그인 응답
    public static class AuthResponse {
        public String token;
        public boolean profileCompleted;
        public long userId;
        public AuthResponse(String t, boolean pc, long id) {
            this.token = t; this.profileCompleted = pc; this.userId = id;
        }
    }

    // 자체 회원가입
    public static class LocalSignupRequest {
        @Email @NotBlank
        public String email;
        @NotBlank @Size(min = 8, max = 64)
        public String password;
        @NotBlank @Size(max = 50)
        public String nickname;
        @NotNull
        public Integer homeRegionId;
        public Integer profileId;
    }

    // 자체 로그인
    public static class LocalLoginRequest {
        @Email @NotBlank
        public String email;
        @NotBlank
        public String password;
    }

    // ✅ 카카오 추가정보 제출용 (지역/닉네임 등)
    public static class CompleteProfileRequest {
        @NotNull public Integer homeRegionId;      // 지역 필수
        @NotBlank @Size(max = 50) public String nickname;
        public Integer profileId;                  // 선택
    }
}

