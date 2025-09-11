package com.penglobe.server.repository;

import com.penglobe.server.domain.diet.DietRecord;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {

    // 특정 사용자의 createdAt이 주어진 기간(startDate ~ endDate) 안에 있는 모든 기록들의 co2Kg 합계
    @Query("""
           SELECT COALESCE(SUM(d.co2Kg), 0)
             FROM DietRecord d
            WHERE d.user.userId = :userId
              AND d.createdAt >= :startDate
              AND d.createdAt < :endDate
           """)
    BigDecimal sumCo2KgByUserAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 특정 사용자(userId)의 하루 기록 전체
    @Query("""
           SELECT d
             FROM DietRecord d
            WHERE d.user.userId = :userId
              AND d.createdAt >= :startOfDay
              AND d.createdAt < :endOfDay
           """)
    List<DietRecord> findByUserUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 하루 기록 개수 (하루 3번 제한 체크용)
    long countByUser_UserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long userId,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    

    void deleteByUser(User user);
}

