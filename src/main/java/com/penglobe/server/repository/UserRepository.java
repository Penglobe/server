package com.penglobe.server.repository;

import com.penglobe.server.domain.user.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 포인트 충분할 때만 차감 → 1건이면 성공, 0건이면 잔액부족
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update User u
              set u.totalPoint = u.totalPoint - :need
            where u.userId = :userId
              and u.totalPoint >= :need
           """)
    int deductPoints(@Param("userId") Long userId, @Param("need") Integer need);
}

