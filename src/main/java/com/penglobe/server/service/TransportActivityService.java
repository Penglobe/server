package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.TransportActivityDto;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class TransportActivityService {

    private final TransportActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final AttendanceLogService attendanceLogService;

    /**
     * 🚶 이동 시작
     */
    @Transactional
    public TransportActivity startActivity(User user, TransportMode mode) {
        TransportActivity activity = TransportActivity.builder()
                .user(user)
                .mode(mode)
                .startTime(LocalDateTime.now())
                .distanceM(0)
                .co2Kg(BigDecimal.ZERO)
                .build();

        return activityRepository.save(activity);
    }

    /**
     * 🏁 이동 종료 (거리, CO₂ 절감량, 포인트, 유저카운터, 출석 로그)
     */
    @Transactional
    public TransportActivityDto stopActivity(Long transportId, int distanceM, String pathGeojson) {
        TransportActivity activity = activityRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));

        activity.setEndTime(LocalDateTime.now());
        activity.setDistanceM(distanceM);

        // 🚩 CO₂ 절감량 계산
        BigDecimal co2Kg = calculateCo2Saving(distanceM, activity.getMode());
        activity.setCo2Kg(co2Kg);

        // ✅ duration 계산
        int durationM = 0;
        if (activity.getStartTime() != null && activity.getEndTime() != null) {
            durationM = (int) Duration.between(activity.getStartTime(), activity.getEndTime()).toMinutes();
        }

        // ✅ 포인트 계산
        int points = co2Kg.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.FLOOR)
                .intValue();

        if (points > 0) {
            User user = activity.getUser();
            user.setTotalPoint(user.getTotalPoint() + points);
            userRepository.save(user);

            PointsLedger ledger = PointsLedger.builder()
                    .user(user)
                    .changeAmount(points)
                    .reason(LedgerReason.TRANSPORT_ACTIVITY)
                    .build();
            pointsLedgerRepository.save(ledger);
        }

        // ✅ UserCounters에 환경걸음 누적 절감량 업데이트
        userCountersRepository.addDistanceCo2(activity.getUser(), co2Kg);

        // 출석 로그 시도 (하루 1회만 인정)
        boolean newAttendance = attendanceLogService.markAttendance(activity.getUser(), AttendanceType.TRANSPORT_ACTIVITY);
        if (newAttendance) {
            log.info("오늘 첫 출석 인정 ✅");
        } else {
            log.info("이미 오늘 출석함 → 무시");
        }

        activityRepository.save(activity);

        return TransportActivityDto.fromEntity(activity, durationM, points);
    }

    /**
     * 📊 CO₂ 절감량 계산 로직
     */
    private BigDecimal calculateCo2Saving(int distanceM, TransportMode mode) {
        BigDecimal km = BigDecimal.valueOf(distanceM)
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);

        // 🚗 승용차 평균 배출량: 0.2 kg/km
        BigDecimal carCo2Kg = km.multiply(BigDecimal.valueOf(0.2));

        // 교통수단별 절감 비율
        double factor = switch (mode) {
            case WALK, BIKE -> 1.0; // 100% 절감
            case TRANSIT -> 0.5;    // 대중교통은 절반만 인정
        };

        return carCo2Kg.multiply(BigDecimal.valueOf(factor))
                .setScale(2, RoundingMode.HALF_UP); // ✅ 소수점 둘째 자리까지
    }
}
