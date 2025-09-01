package com.penglobe.server.service;

import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.TransportActivityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransportActivityService {

    private final TransportActivityRepository activityRepository;

    // 이동 시작
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

    // 이동 종료 (거리, 경로 저장, CO₂ 절감량 계산)
    @Transactional
    public TransportActivity stopActivity(Long transportId, int distanceM, String pathGeojson) {
        TransportActivity activity = activityRepository.findById(transportId)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));

        activity.setEndTime(LocalDateTime.now());
        activity.setDistanceM(distanceM);

        // 🚩 CO₂ 절감량 계산 (소수점 둘째 자리 반올림)
        BigDecimal co2Kg = calculateCo2Saving(distanceM, activity.getMode());
        activity.setCo2Kg(co2Kg);

        // TODO: 유저의 누적 절감량 업데이트
        // TODO: 유저 포인트 지급 로직 추가

        return activityRepository.save(activity);
    }

    // CO₂ 절감량 계산 로직
    private BigDecimal calculateCo2Saving(int distanceM, TransportMode mode) {
        BigDecimal km = BigDecimal.valueOf(distanceM).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);

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
