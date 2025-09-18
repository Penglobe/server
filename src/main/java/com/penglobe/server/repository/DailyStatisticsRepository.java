package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.DailyStatistics;
import com.penglobe.server.dto.survey.StatisticsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long>  {
    // 특정 주차 + 사용자
    List<DailyStatistics> findByYearWeekAndUserIdOrderByDateAsc(int yearWeek, Long userId);

    // 특정 주차 + 전체 통계
    List<DailyStatistics> findByYearWeekAndUserIdIsNullOrderByDateAsc(int yearWeek);

    // ✅ 오늘 + 사용자
    Optional<DailyStatistics> findByUserIdAndDate(Long userId, LocalDate date);

    // ✅ 오늘 + 전체 통계
    Optional<DailyStatistics> findByUserIdIsNullAndDate(LocalDate date);
    // 사용자별 주간 평균
    @Query(value = "SELECT DAYOFWEEK(created_at) AS day_of_week, total_co2kg " +
            "FROM survey_response " +
            "WHERE user_id = :userId AND created_at BETWEEN :startDate AND :endDate " +
            "ORDER BY (DAYOFWEEK(created_at) + 5) % 7", nativeQuery = true)
    List<StatisticsDTO> findWeeklyAvgByUserId(Long userId, LocalDate startDate, LocalDate endDate);

    // 전체 사용자 주간 통계
    @Query(value = "SELECT day_of_week, SUM(statistics_total_co2kg) / SUM(user_count) AS avg_co2 " +
            "FROM survey_statistic " +
            "WHERE user_id IS NULL AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY day_of_week " +
            "ORDER BY (DAYOFWEEK(created_at) + 5) % 7", nativeQuery = true)
    List<StatisticsDTO> findWeeklyAvgAllUsers(LocalDate startDate, LocalDate endDate);

}
