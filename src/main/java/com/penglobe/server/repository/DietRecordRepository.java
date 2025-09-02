package com.penglobe.server.repository;

import com.penglobe.server.domain.diet.DietRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {
    @Query("SELECT COALESCE(SUM(d.co2Kg), 0) FROM DietRecord d WHERE d.user.userId = :userId AND d.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCo2KgByUserAndPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT d FROM DietRecord d WHERE d.user.userId = :userId AND d.createdAt >= :startOfDay AND d.createdAt < :endOfDay")
    List<DietRecord> findByUserUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}

