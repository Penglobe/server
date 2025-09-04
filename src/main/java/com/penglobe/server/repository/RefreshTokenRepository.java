package com.penglobe.server.repository;

import com.penglobe.server.domain.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
