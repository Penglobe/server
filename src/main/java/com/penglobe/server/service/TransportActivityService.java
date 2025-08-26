package com.penglobe.server.service;

import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.TransportActivityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransportActivityService {

    private final TransportActivityRepository activityRepository;

    //이동 시작
    @Transactional
    public TransportActivity startActivity(User user, TransportMode mode) {
        TransportActivity activity = TransportActivity.builder()
                .user(user)
                .mode(mode)
                .startTime(LocalDateTime.now())
                .takenOn(LocalDate.now())
                .distanceM(0)
                .co2Kg(0)
                .build();

        return activityRepository.save(activity);
    }

    //이동 종료 (거리, 경로 저장, CO₂ 절감량 계산)
    @Transactional
    public TransportActivity stopActivity(Long id, int distanceM, String pathGeojson) {
        TransportActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("활동을 찾을 수 없습니다."));

        activity.setEndTime(LocalDateTime.now());
        activity.setDistanceM(distanceM);
        if (pathGeojson != null) {
            activity.setPathGeojson(pathGeojson);
        }

        // 🚩 CO₂ 절감량 계산
        int co2Kg = calculateCo2Saving(distanceM, activity.getMode());
        activity.setCo2Kg(co2Kg);

        return activityRepository.save(activity);
    }

    // CO₂ 절감량 계산 로직
    private int calculateCo2Saving(int distanceM, TransportMode mode) {
        double km = distanceM / 1000.0;
        double carCo2 = km * 140.0 / 1000.0; // g → kg 변환

        double factor = switch (mode) {
            case WALK, BIKE -> 1.0; // 100% 절감
            case TRANSIT -> 0.5;    // 대중교통은 절반만 인정
        };

        return (int) Math.round(carCo2 * factor);
    }
}
