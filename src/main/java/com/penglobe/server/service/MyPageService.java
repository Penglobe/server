package com.penglobe.server.service;

import com.penglobe.server.domain.ranking.WeeklyRankingParticipant;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;
    private final RegionRepository regionRepository;
    private final RankingService rankingService;
    private final WeeklyRankingParticipantRepository weeklyRankingParticipantRepository;
    private final AttendanceLogRepository attendanceLogRepository; // Injected

    public MyPageDTO getMyPageInfo(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        UserCounters userCounters = userCountersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        System.out.println("DEBUG: MyPageService - getMyPageInfo - Fetched UserCounters: totalDays=" + userCounters.getAttendanceTotalDays() + ", longestStreak=" + userCounters.getLongestAttendanceStreak());

        String regionName = null;
        if (user.getRegionId() != null) {
            regionName = regionRepository.findById(user.getRegionId())
                    .map(region -> region.getName())
                    .orElse(null);
        }

        BigDecimal totalScore = userCountersRepository
                .sumTotalCo2ByUserId(userId)
                .orElse(BigDecimal.ZERO);

        System.out.println("DEBUG: MyPageService - User Profile: " + user.getProfile());
        System.out.println("DEBUG: MyPageService - User Region ID: " + user.getRegionId());
        System.out.println("DEBUG: MyPageService - Resolved Region Name: " + regionName);
        return MyPageDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .totalPoint(user.getTotalPoint())
                .attendanceTotalDays(userCounters.getAttendanceTotalDays())
                .longestAttendanceStreak(userCounters.getLongestAttendanceStreak())
                .attendanceStreakDays(userCounters.getAttendanceStreakDays())
                .regionId(user.getRegionId())
                .regionName(regionName)
                .totalScore(totalScore)
                .profile(user.getProfile()) // Added profile field
                .build();
    }

    public DailyCarbonReductionDTO getDailyCarbonReduction(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay(); // Start of the next day

        BigDecimal transportCo2Kg = transportActivityRepository.findByUserUserIdAndStartTimeBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(activity -> Optional.ofNullable(activity.getCo2Kg()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal dietCo2Kg = dietRecordRepository.findByUserUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay)
                .stream()
                .map(record -> Optional.ofNullable(record.getCo2Kg()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCo2Kg = transportCo2Kg.add(dietCo2Kg);

        return DailyCarbonReductionDTO.builder()
                .transportCo2Kg(transportCo2Kg)
                .dietCo2Kg(dietCo2Kg)
                .totalCo2Kg(totalCo2Kg)
                .build();
    }

    public List<String> getAttendanceDates(Long userId) {
        // 1. Fetch sorted, distinct dates directly from the attendance_logs table
        return attendanceLogRepository.findDatesByUserId(userId)
                .stream()
                .sorted()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addDummyData(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        // UserCounters counters = userCountersRepository.findByUserId(userId)
        //         .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        // 더미 TransportActivity 생성 및 수동 출석 업데이트 로직 제거.
        // 이 메서드는 이제 테스트 목적으로 랭킹 업데이트만 트리거합니다.

        // 지속적인 참여 테스트를 위한 더미 지난주 랭크 설정
        user.setLastWeekRank(1);
        userRepository.save(user);

        // 현재 주의 참여자 목록에 사용자가 있는지 확인
        LocalDate currentWeekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        if (!weeklyRankingParticipantRepository.existsByUserIdAndWeekStartDate(userId, currentWeekStartDate)) {
            WeeklyRankingParticipant participant = WeeklyRankingParticipant.builder()
                    .userId(userId)
                    .weekStartDate(currentWeekStartDate)
                    .build();
            weeklyRankingParticipantRepository.save(participant);
        }

        // 즉시 랭킹 업데이트 강제 실행
        rankingService.selectWeeklyParticipants();
        rankingService.updateLiveWeeklyRanking();
        rankingService.updateAllRanking();
        rankingService.updateRegionRankings();
    }

    @Transactional
    public void resetUserAttendanceCounters(Long userId) {
        UserCounters counters = userCountersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        counters.setAttendanceTotalDays(0);
        counters.setLongestAttendanceStreak(0);
        counters.setAttendanceStreakDays(0);
        counters.setLastAttendanceDate(null);
        userCountersRepository.save(counters);
        attendanceLogRepository.deleteByUser(userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId)));
        transportActivityRepository.deleteByUser(userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId)));
        dietRecordRepository.deleteByUser(userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId)));
    }
}