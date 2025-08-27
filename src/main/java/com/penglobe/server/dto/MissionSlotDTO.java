package com.penglobe.server.dto;

import com.penglobe.server.domain.mission.MissionMetric;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record MissionSlotDTO(
        MissionMetric metric, // WALK_CO2_KG / DIET_CO2_KG / ATTEND_TOTAL_DAYS
        long target,          // 이번 칸 목표치 (kg/days)
        BigDecimal progress,        // 현재 진행도 (kg/days)
        int rewardPoints,     // 수령 시 지급 포인트
        boolean locked,       // progress < target
        boolean claimable,    // progress >= target && !claimed
        boolean claimed       // 이미 수령했는지
) {}
