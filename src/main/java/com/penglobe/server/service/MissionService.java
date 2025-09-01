package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.mission.MissionClaim;
import com.penglobe.server.domain.mission.MissionDefinition;
import com.penglobe.server.domain.mission.MissionMetric;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.MissionSlotDTO;
import com.penglobe.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionDefinitionRepository defRepo;
    private final MissionClaimRepository claimRepo;
    private final UserCountersRepository countersRepo;
    private final UserRepository userRepo;
    private final PointsLedgerRepository pointsLedgerRepo;

    /** 10kg/10일 -> 100포인트, 20 -> 200포인트 ... */
    private int rewardOf(long target) {
        return Math.toIntExact(target * 10L);
    }

    private BigDecimal progressOf(UserCounters c, MissionMetric m) {
        return switch (m) {
            case WALK_CO2_KG       -> c.getTotalDistanceCo2Kg();
            case DIET_CO2_KG       -> c.getTotalDietCo2Kg();
            case ATTEND_MONTH_DAYS -> BigDecimal.valueOf(c.getAttendanceMonthDays());
        };
    }

    /** 단일 metric의 4칸 윈도우 (앵커 = 마지막 수령 타겟, 없으면 startTarget) */
    @Transactional(readOnly = true)
    public List<MissionSlotDTO> getWindow(Long userId, MissionMetric metric) {
        UserCounters counters = countersRepo.findById(userId).orElseThrow();

        //한달 출석일 미션
        if (metric == MissionMetric.ATTEND_MONTH_DAYS) {
            String ym = java.time.YearMonth.now().toString(); // "YYYY-MM"
            int monthProgress = counters.getAttendanceMonthDays();
            boolean claimed = claimRepo
                    .findByUserIdAndMetricAndPeriodMonth(userId, metric, ym)
                    .isPresent();

            boolean achieved  = monthProgress >= 20;
            boolean claimable = achieved && !claimed;

            return List.of(MissionSlotDTO.builder()
                    .metric(metric)
                    .target(20)                            // 고정
                    .progress(BigDecimal.valueOf(monthProgress))
                    .rewardPoints(20 * 10)                 // 20일 -> 200포인트 규칙
                    .locked(!achieved)
                    .claimable(claimable)
                    .claimed(claimed)
                    .build());
        }

        //환경걸음, 식단 미션
        MissionDefinition def = defRepo.findByMetric(metric);

        BigDecimal  progress = progressOf(counters, metric);
        long start    = def.getStartTarget();
        long step     = def.getStep();

        Long maxClaimed = claimRepo.findMaxClaimedTarget(userId, metric);
        long anchor = (maxClaimed == null) ? start : Math.max(start, maxClaimed);
        long expected = (maxClaimed == null) ? start : (maxClaimed + step);

        Set<Long> claimedTargets = claimRepo.findByUserIdAndMetric(userId, metric)
                .stream().map(MissionClaim::getTarget).collect(Collectors.toSet());

        List<MissionSlotDTO> list = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            long target = anchor + (i * step);  // 10~40 → (40 수령 후) 40~70
            boolean achieved = progress.compareTo(BigDecimal.valueOf(target)) >= 0;
            boolean claimed  = claimedTargets.contains(target);
            boolean claimable = achieved && !claimed && (target == expected);
            boolean locked    = !achieved;

            list.add(MissionSlotDTO.builder()
                    .metric(metric)
                    .target(target)
                    .progress(progress)
                    .rewardPoints(rewardOf(target))
                    .locked(locked)
                    .claimable(claimable)
                    .claimed(claimed)
                    .build());
        }
        return list;
    }

    /** 3개 metric 모두 반환 */
    @Transactional(readOnly = true)
    public Map<MissionMetric, List<MissionSlotDTO>> getWindows(Long userId) {
        Map<MissionMetric, List<MissionSlotDTO>> res = new EnumMap<>(MissionMetric.class);
        for (MissionMetric m : MissionMetric.values()) {
            res.put(m, getWindow(userId, m));
        }
        return res;
    }

    /** 수령 (중복/동시성: UNIQUE(user, metric, target)로 멱등 보장) */
    @Transactional
    public void claim(Long userId, MissionMetric metric, long target) {
        UserCounters counters = countersRepo.findById(userId).orElseThrow();

        MissionDefinition def = defRepo.findByMetric(metric);
        long start = def.getStartTarget();
        long step  = def.getStep();

        // 정의 조회 및 타겟 유효성 검증
        if (target < start || (target - start) % step != 0) {
            throw new IllegalArgumentException("유효하지 않은 목표치입니다."); // 등차수열 밖
        }

        // 기존 진행도 검증
        BigDecimal progress = progressOf(counters, metric);
        if (progress.compareTo(BigDecimal.valueOf(target)) < 0) throw new IllegalArgumentException("아직 목표치에 도달하지 않았습니다.");

        // 이미 수령한 타겟(멱등) → 조용히 성공 처리
        if (claimRepo.existsByUserIdAndMetricAndTarget(userId, metric, target)) {
            return; // 멱등: 200 OK, 추가 변화 없음
        }

        // 연속 수령 강제
        Long max = claimRepo.findMaxClaimedTarget(userId, metric);
        long expected = (max == null) ? start : (max + step);
        if (target != expected) {
            throw new IllegalArgumentException("이전 단계를 먼저 수령해 주세요."); // 바로 다음 칸만 허용
        }

        try {
            MissionClaim claim = claimRepo.save(
                    MissionClaim.builder()
                            .userId(userId)
                            .metric(metric)
                            .target(target)
                            .build()
            );

            int reward = rewardOf(target);

            // 포인트 지급 연동 추가해야함
            User user = userRepo.findById(userId).orElseThrow();
            int newBalance = user.getTotalPoint() + reward;

            pointsLedgerRepo.save(
                    PointsLedger.builder()
                            .user(user)
                            .changeAmount(reward)               // +포인트
                            .reason(LedgerReason.MISSION_REWARD) // 사유(레저 enum)
                            .build()
            );

            user.setTotalPoint(newBalance); // 보유 포인트 갱신
            // pointsService.add(userId, def.getRewardPoints(), "MISSION_CLAIM", metric + ":" + target);

        } catch (DataIntegrityViolationException ignore) {
            // 이미 수령됨 → 멱등 처리
        }
    }
}

