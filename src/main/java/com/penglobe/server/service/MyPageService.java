package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceLog;
import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.ranking.WeeklyRankingParticipant;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.repository.*;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final TransportActivityService transportActivityService;
    private final AttendanceLogService attendanceLogService;

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
        // 1. Fetch datetime lists from both repositories
        List<LocalDateTime> transportDates = transportActivityRepository.findStartTimeByUserId(userId);
        List<LocalDateTime> dietDates = dietRecordRepository.findCreatedAtByUserId(userId);

        // 2. Combine, convert to LocalDate, and remove duplicates
        return Stream.concat(transportDates.stream(), dietDates.stream())
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .sorted()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addDummyData(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        UserCounters counters = userCountersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        List<LocalDate> datesToAdd = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            datesToAdd.add(LocalDate.now().minusDays(i));
        }
        // Process dates in chronological order for correct streak calculation
        Collections.sort(datesToAdd);

        for (LocalDate date : datesToAdd) {
            // 1. Create dummy TransportActivity
            LocalDateTime activityTime = date.atStartOfDay().plusHours(9);
            TransportActivity dummyActivity = TransportActivity.builder()
                    .user(user)
                    .mode(TransportMode.WALK)
                    .startTime(activityTime)
                    .endTime(activityTime.plusMinutes(30))
                    .distanceM(1000)
                    .co2Kg(BigDecimal.valueOf(1.5))
                    .build();
            TransportActivity savedActivity = transportActivityRepository.save(dummyActivity);

            // Call stopActivity to calculate points and update user's totalPoint
            transportActivityService.stopActivity(savedActivity.getTransportId(), savedActivity.getDistanceM(), null);

            // 2. Mark attendance (logic copied and adapted from AttendanceLogService)
            // Check if attendance for this day already exists
            if (!attendanceLogRepository.existsByUserAndDate(user, date)) {
                // Save new attendance log
                AttendanceLog log = AttendanceLog.builder()
                        .user(user)
                        .date(date)
                        .attendanceType(AttendanceType.TRANSPORT_ACTIVITY)
                        .shownAt(date)
                        .build();
                attendanceLogRepository.save(log);
            }

                // ONLY update counters IF a new attendance log was created
                counters.setAttendanceTotalDays(counters.getAttendanceTotalDays() + 1);

                if (date.equals(counters.getLastAttendanceDate() != null ? counters.getLastAttendanceDate().plusDays(1) : null)) {
                    // Consecutive day
                    counters.setAttendanceStreakDays(counters.getAttendanceStreakDays() + 1);
                } else {
                    // Streak is broken or it's the first attendance
                    counters.setAttendanceStreakDays(1);
                }

                if (counters.getAttendanceStreakDays() > counters.getLongestAttendanceStreak()) {
                    counters.setLongestAttendanceStreak(counters.getAttendanceStreakDays());
                }
                counters.setLastAttendanceDate(date);
            }
        userCountersRepository.flush();
        userCountersRepository.save(counters); // Save all counter updates at the end

        // Set a dummy last week rank for testing persistent participation
        user.setLastWeekRank(1);
        userRepository.save(user);

        // Ensure user is in current week's participant list
        LocalDate currentWeekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        if (!weeklyRankingParticipantRepository.existsByUserIdAndWeekStartDate(userId, currentWeekStartDate)) {
            WeeklyRankingParticipant participant = WeeklyRankingParticipant.builder()
                    .userId(userId)
                    .weekStartDate(currentWeekStartDate)
                    .build();
            weeklyRankingParticipantRepository.save(participant);
        }

        // Force update rankings immediately
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