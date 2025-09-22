package com.penglobe.server.service;

import com.penglobe.server.domain.user.RefreshToken;
import com.penglobe.server.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long REFRESH_EXPIRE_SECONDS = 60L * 60L * 24L * 30L;

    private final RefreshTokenRepository refreshTokenRepository;

    /** 로그인/재발급 시 새 RT 발급 (기존 것 삭제 → 로테이션) */
    @Transactional
    public String issueFor(Long userId) {
        final long now = Instant.now().toEpochMilli();
        final long expiry = now + (REFRESH_EXPIRE_SECONDS * 1000L);
        final String token = UUID.randomUUID().toString();

        // 유저별 단일 RT만 유지
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken rt = RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiryTime(expiry)
                .build();
        refreshTokenRepository.save(rt);
        return token;
    }

    /** RT 검증 → 유효하면 userId 반환, 아니면 null */
    @Transactional(readOnly = true)
    public Long validateAndGetUserId(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> rt.getExpiryTime() > Instant.now().toEpochMilli())
                .map(RefreshToken::getUserId)
                .orElse(null);
    }

    /** 유저의 모든 RT 폐기 (로그아웃 등) */
    @Transactional
    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
