package com.penglobe.server.service;

import com.penglobe.server.domain.ranking.WeeklyRankingParticipant;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.repository.RegionRepository;
import com.penglobe.server.repository.WeeklyRankingParticipantRepository;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Optional;

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
    private final WeeklyRankingParticipantRepository weeklyRankingParticipantRepository; // Injected

    public MyPageDTO getMyPageInfo(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        UserCounters userCounters = userCountersRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user ID: " + userId));

        String regionName = null;
        if (user.getRegionId() != null) {
            regionName = regionRepository.findById(user.getRegionId())
                    .map(region -> region.getName())
                    .orElse(null);
        }

        return MyPageDTO.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .totalPoint(user.getTotalPoint())
                .attendanceTotalDays(userCounters.getAttendanceTotalDays())
                .longestAttendanceStreak(userCounters.getLongestAttendanceStreak())
                .attendanceStreakDays(userCounters.getAttendanceStreakDays())
                .regionId(user.getRegionId())
                .regionName(regionName)
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
                .map(record -> Optional.ofNullable(record.getCo2Kg()).orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCo2Kg = transportCo2Kg.add(dietCo2Kg);

        return DailyCarbonReductionDTO.builder()
                .transportCo2Kg(transportCo2Kg)
                .dietCo2Kg(dietCo2Kg)
                .totalCo2Kg(totalCo2Kg)
                .build();
    }

    @Transactional
    public void addDummyData(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        UserCounters userCounters = userCountersRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters not found for user: " + user.getNickname()));

        // 1. Modify the entity in memory
        BigDecimal currentDistanceCo2 = Optional.ofNullable(userCounters.getTotalDistanceCo2Kg()).orElse(BigDecimal.ZERO);
        userCounters.setTotalDistanceCo2Kg(currentDistanceCo2.add(BigDecimal.TEN));
        userCounters.setLastAttendanceDate(LocalDate.now());

        // Set a dummy last week rank for testing persistent participation
        user.setLastWeekRank(1); // Simulate having ranked last week
        userRepository.save(user); // Save the user entity to persist the lastWeekRank

        // 2. Save UserCounters changes
        userCountersRepository.save(userCounters);

        // --- NEW: Create a dummy TransportActivity record for weekly ranking calculation ---
        TransportActivity dummyActivity = TransportActivity.builder()
                .user(user)
                .mode(TransportMode.WALK) // Use a valid mode
                .startTime(LocalDateTime.now().minusMinutes(10)) // Start 10 mins ago
                .endTime(LocalDateTime.now())
                .distanceM(1000) // 1km
                .co2Kg(BigDecimal.TEN) // Add 10kg CO2 for this activity
                .build();
        transportActivityRepository.save(dummyActivity);
        // --- END NEW ---

        // --- Ensure user is in current week's participant list ---
        LocalDate currentWeekStartDate = LocalDate.now().with(DayOfWeek.MONDAY);
        // Create or update the participant entry for the current user for this week
        // This ensures the user is always included in the current week's ranking cohort
        WeeklyRankingParticipant participant = WeeklyRankingParticipant.builder()
                .userId(userId)
                .weekStartDate(currentWeekStartDate)
                .build();
        weeklyRankingParticipantRepository.save(participant);
        // --- END Ensure ---

        // 3. Force update rankings immediately
        rankingService.updateLiveWeeklyRanking();
        rankingService.updateAllRanking();
        rankingService.updateRegionRankings();
    }
}
