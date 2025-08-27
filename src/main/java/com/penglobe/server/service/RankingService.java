package com.penglobe.server.service;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.PersonalRankingResponseDTO;
import com.penglobe.server.dto.RankedUserDTO;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal; // Added import

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;

    /**
     * 사용자의 주간 랭킹 정보를 조회합니다.
     *
     * 1. 새로운 주가 시작되었는지 확인하고, 지난 주 랭킹을 최종 집계하여 저장합니다.
     * 2. 사용자가 이번 주 랭킹 그룹에 속해있지 않다면, 활동 자격을 확인한 후 새로운 그룹을 생성하고 할당합니다.
     * 3. 사용자가 속한 그룹의 현재 랭킹을 계산하여 반환합니다.
     *
     * @param currentUserId 현재 사용자의 ID
     * @return PersonalRankingResponseDTO 사용자의 지난 주 랭킹과 현재 주간 랭킹 목록
     */
    @Transactional
    public PersonalRankingResponseDTO getPersonalWeeklyRanking(Long currentUserId) {
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // --- 1. 지난 주 랭킹 집계 및 초기화 ---
        // 새로운 주가 시작되었다면 (그룹 ID에 저장된 날짜가 이번 주 시작일 이전이라면), 지난 주 랭킹을 최종 집계하고 현재 랭킹 그룹 정보를 초기화합니다.
        String groupId = currentUser.getWeeklyRankingGroupId();
        if (groupId != null) {
            LocalDate groupAssignedDate = LocalDate.parse(groupId.substring(0, 10)); // "YYYY-MM-DD" 형식의 날짜 파싱
            if (groupAssignedDate.isBefore(thisWeekStart)) {
                finalizeAndSaveLastWeekRanking(currentUser, lastWeekStart);
                currentUser.setWeeklyRankingGroupId(null);
            }
        }

        // --- 2. 이번 주 랭킹 그룹 조회 ---
        // 스케줄러에 의해 이미 그룹이 할당되어 있다고 가정합니다.
        List<User> rankingGroup;
        if (currentUser.getWeeklyRankingGroupId() == null) {
            // 그룹이 할당되지 않았다면 (스케줄러에 의해 배정되지 않았거나 지난 주 그룹이 초기화된 경우),
            // 해당 유저는 이번 주 랭킹 그룹에 포함되지 않습니다.
            return new PersonalRankingResponseDTO(currentUser.getLastWeekRank(), Collections.emptyList());
        } else {
            // 그룹이 할당되어 있다면 해당 그룹 정보를 조회합니다.
            rankingGroup = userRepository.findByWeeklyRankingGroupId(currentUser.getWeeklyRankingGroupId());
        }

        // --- 3. 현재 랭킹 계산 ---
        // 최종적으로 구성된 랭킹 그룹을 기준으로 현재 시점의 랭킹을 계산합니다.
        List<RankedUserDTO> currentRanking = calculateCurrentRanking(rankingGroup, thisWeekStart, today, currentUserId);

        return new PersonalRankingResponseDTO(currentUser.getLastWeekRank(), currentRanking);
    }

    private void finalizeAndSaveLastWeekRanking(User user, LocalDate lastWeekStart) {
        String lastWeekGroupId = user.getWeeklyRankingGroupId();
        if (lastWeekGroupId == null) return;

        List<User> lastWeekGroup = userRepository.findByWeeklyRankingGroupId(lastWeekGroupId);
        if (lastWeekGroup.isEmpty()) return;

        LocalDate lastWeekEnd = lastWeekStart.plusDays(7);

        Map<Long, Long> userScores = new HashMap<>();
        for (User member : lastWeekGroup) {
            long score = calculateTotalCo2ForPeriod(member.getId(), lastWeekStart, lastWeekEnd);
            userScores.put(member.getId(), score);
        }

        List<Map.Entry<Long, Long>> sortedScores = userScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // userId를 key로 갖는 Map을 만들어 User 객체를 찾음
        Map<Long, User> userMap = lastWeekGroup.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 랭킹 부여 로직
        int rank = 1;
        for (int i = 0; i < sortedScores.size(); i++) {
            if (i > 0 && !sortedScores.get(i).getValue().equals(sortedScores.get(i - 1).getValue())) {
                rank = i + 1;
            }
            Long userId = sortedScores.get(i).getKey();
            User member = userMap.get(userId);
            if (member != null) {
                member.setLastWeekRank(rank);
            }
        }
        userRepository.saveAll(lastWeekGroup);
    }

    

    private List<RankedUserDTO> calculateCurrentRanking(List<User> group, LocalDate weekStart, LocalDate today, Long currentUserId) {
        Map<User, Long> userScores = new HashMap<>();
        for (User user : group) {
            long score = calculateTotalCo2ForPeriod(user.getId(), weekStart, today.plusDays(1));
            userScores.put(user, score);
        }

        List<Map.Entry<User, Long>> sortedScores = userScores.entrySet().stream()
                .sorted(Map.Entry.<User, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        List<RankedUserDTO> rankedList = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sortedScores.size(); i++) {
            if (i > 0 && !sortedScores.get(i).getValue().equals(sortedScores.get(i - 1).getValue())) {
                rank = i + 1;
            }
            Map.Entry<User, Long> entry = sortedScores.get(i);
            User user = entry.getKey();
            rankedList.add(new RankedUserDTO(
                    user.getId(),
                    user.getNickname(),
                    rank,
                    java.math.BigDecimal.valueOf(entry.getValue()), // Convert Long to BigDecimal
                    user.getId().equals(currentUserId)
            ));
        }
        return rankedList;
    }

    private long calculateTotalCo2ForPeriod(Long userId, LocalDate startDate, LocalDate endDate) {
        long transportCo2 = transportActivityRepository.sumCo2KgByUserIdAndTakenOnBetween(userId, startDate, endDate).orElse(0L);
        long dietCo2 = dietRecordRepository.sumCo2KgByUserIdAndTakenOnBetween(userId, startDate, endDate).orElse(0L);
        return transportCo2 + dietCo2;
    }

    /**
     * 전체 사용자의 누적 총 절감량을 기준으로 순위를 조회합니다.
     *
     * @return 순위가 포함된 RankedUserDTO 리스트
     */
    public List<RankedUserDTO> getOverallRanking() {
        List<RankedUserDTO> rankedUsers = userRepository.findAllUsersWithTotalSavingsForRanking();

        // 순위 로직 적용
        int rank = 1;
        for (int i = 0; i < rankedUsers.size(); i++) {
            // BigDecimal 비교는 compareTo() 사용
            if (i > 0 && rankedUsers.get(i).getTotalCo2Saved().compareTo(rankedUsers.get(i - 1).getTotalCo2Saved()) != 0) {
                rank = i + 1;
            }
            rankedUsers.get(i).setRank(rank);
        }
        return rankedUsers;
    }
}