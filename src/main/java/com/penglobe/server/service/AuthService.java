package com.penglobe.server.service;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.AuthDTO.*;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient = RestClient.create();

    //카카오 로그인
    //앱에서 받은 accessToken으로 /v2/user/me 호출
    public AuthResponse loginWithKakao (String accessToken){
        Map<?,?> me = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer" + accessToken)
                .retrieve()
                .body(Map.class);

        Long kakaoId = ((Number) me.get("id")).longValue();

        User user = userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User u = User.builder()
                    .kakaoId(kakaoId)
                    .isProfileComplete(false)
                    .build();
            return userRepository.save(u);
        });

        String jwt = issueJwt(user.getId());
        return new AuthResponse(jwt, Boolean.TRUE.equals(user.getIsProfileComplete()), user.getId());
    }

    //자체 회원가입
    public void signupLocal(LocalSignupRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        User u = User.builder()
                .email(req.email)
                .passwordHash(passwordEncoder.encode(req.password))
                .nickname(req.nickname)
                .homeRegionId(req.homeRegionId)
                .profileId(req.profileId)
                .isProfileComplete(true)
                .build();
        userRepository.save(u);
    }

    //자체 로그인
    public AuthResponse loginLocal(LocalLoginRequest req) {
        User u = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        if (u.getPasswordHash() == null || !passwordEncoder.matches(req.password, u.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String jwt = issueJwt(u.getId());
        return new AuthResponse(jwt, Boolean.TRUE.equals(u.getIsProfileComplete()), u.getId());
    }

    // 카카오용 프로필 완료 API: 지역/닉네임 받아 complete=true
    public void completeProfile(long userId, CompleteProfileRequest req) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setHomeRegionId(req.homeRegionId);
        u.setNickname(req.nickname);
        if (req.profileId != null) u.setProfileId(req.profileId);
        u.setIsProfileComplete(true);
        userRepository.save(u);
    }

    private String issueJwt(Long userId) {
        return "dummy.jwt.for.user." + userId;
    }
}
