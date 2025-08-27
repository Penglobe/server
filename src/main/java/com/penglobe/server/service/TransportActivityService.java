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

        // 유저의 환경걸음을 통한 누적 절감량 업데이트
        
        // 유저 포인트 주기

        return activityRepository.save(activity);
    }

    // CO₂ 절감량 계산 로직
    private int calculateCo2Saving(int distanceM, TransportMode mode) {
        double km = distanceM / 1000.0;

        // 🚗 승용차 평균 배출량: 200 g/km = 0.2 kg/km
        double carCo2Kg = km * 0.2;

        // 교통수단별 절감 비율
        double factor = switch (mode) {
            case WALK, BIKE -> 1.0; // 100% 절감
            case TRANSIT -> 0.5;    // 대중교통은 절반만 인정
        };

        // 반올림해서 int로 변환
        // db에 저장은 반올림한 값으로 하지만 프론트에서는 거리를 이용해 소수점까지 표현하기.
        return (int) Math.round(carCo2Kg * factor);
    }


}
