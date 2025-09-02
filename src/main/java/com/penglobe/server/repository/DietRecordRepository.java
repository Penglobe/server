package com.penglobe.server.repository;

import com.penglobe.server.domain.diet.DietRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {
    @Query("SELECT COALESCE(SUM(d.co2Kg), 0) FROM DietRecord d WHERE d.user.userId = :userId AND d.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumCo2KgByUserAndPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<DietRecord> findByUserUserIdAndCreatedAt(Long userId, LocalDateTime createdAt);
}

