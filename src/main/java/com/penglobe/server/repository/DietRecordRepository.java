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

public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {

    /**
     * 특정 기간 내에 해당 사용자의 식단 기록이 존재하는지 확인합니다.
     * @param userId 사용자 ID
     * @param start 시작일
     * @param end 종료일
     * @return 기록 존재 여부
     */
    boolean existsByUserIdAndTakenOnBetween(Long userId, LocalDate start, LocalDate end);

    /**
     * 특정 기간 내에 해당 사용자의 식단 기록으로 절약된 총 CO2 양을 계산합니다.
     * @param userId 사용자 ID
     * @param start 시작일
     * @param end 종료일
     * @return Optional<Long> 형태의 총 CO2 절약량
     */
    @Query("SELECT SUM(d.co2Kg) FROM DietRecord d WHERE d.user.id = :userId AND d.takenOn >= :start AND d.takenOn < :end")
    Optional<Long> sumCo2KgByUserIdAndTakenOnBetween(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    /**
     * 특정 기간 내에 식단 기록이 있는 모든 사용자의 ID를 중복 없이 조회합니다.
     * @param start 시작일
     * @param end 종료일
     * @return 사용자 ID Set
     */
    @Query("SELECT DISTINCT d.user.id FROM DietRecord d WHERE d.takenOn >= :start AND d.takenOn < :end")
    Set<Long> findDistinctUserIdsWithActivityBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
