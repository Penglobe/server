package com.penglobe.server.repository;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TransportActivityRepository extends JpaRepository<TransportActivity, Long> {

    /**
     * 특정 기간 내에 해당 사용자의 활동 기록이 존재하는지 확인합니다.
     * createdAt 기준
     */
    boolean existsByUserUserIdAndCreatedAtBetween(Long userId,
                                                  LocalDateTime start,
                                                  LocalDateTime end);

    /**
     * 특정 기간 내에 해당 사용자의 활동으로 절약된 총 CO2 양을 계산합니다.
     * createdAt 기준
     */
    @Query("SELECT SUM(t.co2Kg) " +
            "FROM TransportActivity t " +
            "WHERE t.user.userId = :userId " +
            "AND t.createdAt >= :start " +
            "AND t.createdAt < :end")
    Optional<BigDecimal> sumCo2KgByUserIdAndCreatedAtBetween(@Param("userId") Long userId,
                                                             @Param("start") LocalDateTime start,
                                                             @Param("end") LocalDateTime end);

    /**
     * 특정 기간 내에 활동 기록이 있는 모든 사용자 ID를 중복 없이 조회합니다.
     * createdAt 기준
     */
    @Query("SELECT DISTINCT t.user.userId " +
            "FROM TransportActivity t " +
            "WHERE t.createdAt >= :start " +
            "AND t.createdAt < :end")
    Set<Long> findDistinctUserIdsWithActivityBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    // Added method for MyPageService
    @Query("SELECT t FROM TransportActivity t WHERE t.user.userId = :userId AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    List<TransportActivity> findByUserUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    

    @Query("SELECT t FROM TransportActivity t WHERE t.user.userId = :userId AND t.startTime >= :startOfDay AND t.startTime < :endOfDay")
    List<TransportActivity> findByUserUserIdAndStartTimeBetween(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    void deleteByUser(User user);
}