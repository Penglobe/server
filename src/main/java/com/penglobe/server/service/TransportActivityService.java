package com.penglobe.server.service;
import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserCountersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TransportActivityService {

    private final TransportActivityRepository activityRepository;
    private final PointsLedgerRepository ledgerRepository;
    private final UserCountersRepository countersRepository;

    // 1) 이동 시작
    @Transactional
    public TransportActivity startActivity(User user, TransportMode mode) {
        TransportActivity activity = TransportActivity.builder()
                .user(user)
                .mode(mode)
                .startTime(java.time.LocalDateTime.now())
                .takenOn(LocalDate.now())
                .build();
        return activityRepository.save(activity);
    }

    // 2) 이동 종료 + CO2 계산 + Ledger 기록
    @Transactional
    public TransportActivity stopActivity(Long activityId, int distanceM, String pathGeojson) {
        TransportActivity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));

        activity.setEndTime(java.time.LocalDateTime.now());
        activity.setDistanceM(distanceM);
        activity.setPathGeojson(pathGeojson);

        // CO2 절감량 = km × 140g
        double km = distanceM / 1000.0;
        int co2 = (int) (km * 140);

        if (activity.getMode() == TransportMode.TRANSIT) {
            co2 = (int) (co2 * 0.3); // 대중교통은 가중치 감소
        }

        activity.setCo2g(co2);

        // Ledger 기록
        int rewardPoints = co2 / 10; // 예: 절감량 10g당 1포인트
        int balance = updateUserPoints(activity.getUser(), rewardPoints);

        PointsLedger ledger = PointsLedger.builder()
                .user(activity.getUser())
                .eventDate(LocalDate.now())
                .changeAmount(rewardPoints)
                .reason(LedgerReason.WALK_ACTIVITY)
                .refTable("transport_activities")
                .refId(activity.getId())
                .balanceAfter(balance)
                .build();
        ledgerRepository.save(ledger);

        // 누적 카운터 업데이트
        UserCounters counters = countersRepository.findById(activity.getUser().getId())
                .orElse(UserCounters.builder().user(activity.getUser()).build());
        counters.setTotalDistanceM(counters.getTotalDistanceM() + distanceM);
        countersRepository.save(counters);

        return activity;
    }

    private int updateUserPoints(User user, int rewardPoints) {
        int newBalance = user.getTotalPoint() + rewardPoints;
        user.setTotalPoint(newBalance);
        return newBalance;
    }
}
