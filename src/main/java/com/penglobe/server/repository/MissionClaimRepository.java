package com.penglobe.server.repository;

import com.penglobe.server.domain.mission.MissionClaim;
import com.penglobe.server.domain.mission.MissionMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MissionClaimRepository extends JpaRepository<MissionClaim, Long> {
    boolean existsByUserIdAndMetricAndTarget(Long userId, MissionMetric metric, Long target);
    boolean existsByUserIdAndMetricAndPeriodMonth(Long userId, MissionMetric metric, String periodMonth);

    List<MissionClaim> findByUserIdAndMetric(Long userId, MissionMetric metric);

    @Query("""
        select max(c.target)
        from MissionClaim c
        where c.userId = :userId and c.metric = :metric
    """)
    Long findMaxClaimedTarget(@Param("userId") Long userId, @Param("metric") MissionMetric metric);

    Optional<MissionClaim> findByUserIdAndMetricAndPeriodMonth(Long userId, MissionMetric metric, String periodMonth);

}
