package com.penglobe.server.repository;

import com.penglobe.server.domain.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByKakaoId(Long kakaoId);
    boolean existsByEmail(String email);
    boolean existsByKakaoId(Long kakaoId);

    @Modifying
    @Transactional
    @Query("update User u set u.lastWeekRank = null")
    void resetAllLastWeekRanks();
    Optional<User> findByUserId(Long userId);
}

