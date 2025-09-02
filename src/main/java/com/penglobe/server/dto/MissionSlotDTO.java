package com.penglobe.server.dto;

import com.penglobe.server.domain.mission.MissionMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionSlotDTO {
    private MissionMetric metric;   // WALK_CO2_KG / DIET_CO2_KG / ATTEND_TOTAL_DAYS
    private long target;            // 이번 칸 목표치 (kg/days)
    private BigDecimal progress;    // 현재 진행도 (kg/days)
    private int rewardPoints;       // 수령 시 지급 포인트
    private boolean locked;         // progress < target
    private boolean claimable;      // progress >= target && !claimed
    private boolean claimed;        // 이미 수령했는지
}
