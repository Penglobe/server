package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
