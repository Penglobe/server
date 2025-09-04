package com.penglobe.server.service;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.AuthDTO.*;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestClient restClient = RestClient.create();
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final UserCountersRepository userCountersRepository;

    //카카오 로그인
    //앱에서 받은 accessToken으로 /v2/user/me 호출
    public AuthResponse loginWithKakao (String kakaoAccessToken){
        Map<?,?> me = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(Map.class);

        if (me == null || me.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Kakao /v2/user/me returned no id: " + String.valueOf(me));
        }

        Long kakaoId = ((Number) me.get("id")).longValue();

        User user = userRepository.findByKakaoId(kakaoId).orElseGet(() -> {
            User u = User.builder()
                    .kakaoId(kakaoId)
                    .isProfileComplete(false)
                    .build();
            u = userRepository.save(u);
            // 신규 생성 시 즉시 카운터도 생성
            userCountersRepository.save(UserCounters.builder().user(u).build());
            return u;
        });
        // 혹시 예전 사용자 중 카운터가 없는 경우 대비해 한 번 더 보정
        if (!userCountersRepository.existsById(user.getUserId())) {
            userCountersRepository.save(UserCounters.builder().user(user).build());
        }

        String accessToken = jwtTokenProvider.createToken(user.getUserId(), "USER");
        String refreshToken = tokenService.issueFor(user.getUserId());
        return new AuthResponse(accessToken,
                refreshToken,
                Boolean.TRUE.equals(user.getIsProfileComplete()),
                user.getUserId());
    }

    //자체 회원가입
    @Transactional
    public void signupLocal(LocalSignupRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        User u = User.builder()
                .email(req.email)
                .passwordHash(passwordEncoder.encode(req.password))
                .nickname(req.nickname)
                .regionId(req.regionId)
                .profileId(req.profileId)
                .isProfileComplete(true)
                .build();
        userRepository.save(u);

        // 여기서 카운터 1:1 생성 (공유 PK: @MapsId)
        if (!userCountersRepository.existsById(u.getUserId())) {
            userCountersRepository.save(UserCounters.builder().user(u).build());
        }
    }

    //자체 로그인
    public AuthResponse loginLocal(LocalLoginRequest req) {
        User user = userRepository.findByEmail(req.email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다."));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(req.password, user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        String accessToken = jwtTokenProvider.createToken(user.getUserId(), "USER");
        String refreshToken = tokenService.issueFor(user.getUserId());
        return new AuthResponse(accessToken, refreshToken, Boolean.TRUE.equals(user.getIsProfileComplete()), user.getUserId());
    }

    // 카카오용 프로필 완료 API: 지역/닉네임 받아 complete=true
    public void completeProfile(long userId, CompleteProfileRequest req) {
        User u = userRepository.findById(userId).orElseThrow();
        u.setRegionId(req.regionId);
        u.setNickname(req.nickname);
        if (req.profileId != null) u.setProfileId(req.profileId);
        u.setIsProfileComplete(true);
        userRepository.save(u);
    }

    private String issueJwt(Long userId) {
        return "dummy.jwt.for.user." + userId;
    }
}
