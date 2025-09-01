package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    //사용자의 설문 응답 저장
    //총 co2, 제출일자, 설문 저장
    SurveyResponse findTopByUserIdOrderBySurveyDateDesc(Long userId);

    @Query("SELECT COALESCE(SUM(s.totalCo2kg), 0) FROM SurveyResponse s WHERE s.userId = :userId AND s.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalCo2KgByUserAndPeriod(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
