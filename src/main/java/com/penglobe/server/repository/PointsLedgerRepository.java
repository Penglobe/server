package com.penglobe.server.repository;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import org.springframework.cglib.core.Local;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    //사용자 id로 포인트 사용내역 조회
    @Query("SELECT p FROM PointsLedger p WHERE p.user = :user AND p.eventDate BETWEEN :from AND :to ORDER BY p.eventDate DESC")
    List<PointsLedger> findByUserAndEventDateBetweenOrderByEventDateDesc(
            @Param("user") User user,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
    //현재 잔액 조회
    PointsLedger findTopByUserOrderByEventDateDesc(User user);
}
