package com.penglobe.server.repository;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;


public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {

    // 사용자별 전체 포인트 내역 조회 (최근 내역이 먼저)
    List<PointsLedger> findByUserOrderByCreatedAtDesc(User user);

    // 최신 레저 조회
    PointsLedger findTopByUserOrderByCreatedAtDesc(User user);

    //중복 제출 조회
    @Query("SELECT COUNT(p) FROM PointsLedger p " +
            "WHERE p.user.userId = :userId " +
            "AND DATE(p.createdAt) = CURRENT_DATE " +
            "AND p.reason = 'QUIZ'")
    long countTodayQuizSubmit(@Param("userId") Long userId);

}