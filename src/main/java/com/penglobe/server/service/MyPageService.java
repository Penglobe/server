package com.penglobe.server.service;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;

    public MyPageDTO getMyPageInfo(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        UserCounters userCounters = userCountersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        return MyPageDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .totalPoint(user.getTotalPoint())
                .attendanceTotalDays(userCounters.getAttendanceTotalDays())
                .longestAttendanceStreak(userCounters.getLongestAttendanceStreak())
                .attendanceStreakDays(userCounters.getAttendanceStreakDays())
                .build();
    }

    public DailyCarbonReductionDTO getDailyCarbonReduction(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // Start of the next day

        BigDecimal transportCo2Kg = transportActivityRepository.findByUserUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(activity -> Optional.ofNullable(activity.getCo2Kg()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dietCo2Kg = dietRecordRepository.findByUserUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(record -> BigDecimal.valueOf(Optional.ofNullable(record.getCo2Kg()).orElse(0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCo2Kg = transportCo2Kg.add(dietCo2Kg);

        return DailyCarbonReductionDTO.builder()
                .transportCo2Kg(transportCo2Kg)
                .dietCo2Kg(dietCo2Kg)
                .totalCo2Kg(totalCo2Kg)
                .build();
    }
}
