package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {

    @Query("SELECT COALESCE(SUM(s.totalCo2kg), 0) FROM SurveyResponse s WHERE s.userId = :userId AND s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalCo2KgByUserAndPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 제출 여부 확인
    boolean existsByUserIdAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);

    List<SurveyResponse> findByUserIdAndCreatedAtBetween(Long userId, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);
}
