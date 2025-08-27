package com.penglobe.server.scheduler;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingGroupScheduler {

    private final UserRepository userRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;

    /**
     * 매주 월요일 자정에 실행되어 주간 랭킹 그룹을 배정합니다.
     * 지난 주 활동 기록이 있는 사용자들을 대상으로 그룹을 형성하고,
     * 각 사용자에게 새로운 주간 랭킹 그룹 ID를 할당합니다.
     */
    @Scheduled(cron = "0 0 0 * * MON") // 매주 월요일 00시 00분 00초에 실행
    @Transactional
    public void assignWeeklyRankingGroups() {
        log.info("주간 랭킹 그룹 배정 스케줄러 시작: {}", LocalDate.now());

        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        LocalDate lastWeekStart = thisWeekStart.minusWeeks(1);
        LocalDate eligibilityEnd = thisWeekStart; // 지난 주 월요일부터 이번 주 월요일까지의 활동 기준

        // 1. 지난 주 활동 기록이 있는 모든 사용자 ID를 조회 (랭킹 참여 자격이 있는 사용자)
        Set<Long> transportUserIds = transportActivityRepository.findDistinctUserIdsWithActivityBetween(lastWeekStart, eligibilityEnd);
        Set<Long> dietUserIds = dietRecordRepository.findDistinctUserIdsWithActivityBetween(lastWeekStart, eligibilityEnd);

        Set<Long> eligibleUserIdsSet = new HashSet<>(transportUserIds);
        eligibleUserIdsSet.addAll(dietUserIds);

        List<User> eligibleUsers = userRepository.findAllById(eligibleUserIdsSet);
        Collections.shuffle(eligibleUsers); // 그룹 배정을 위해 사용자 목록을 섞음

        int groupSize = 10; // 한 그룹당 최대 인원
        int groupCount = 0; // 생성된 그룹의 순번

        for (int i = 0; i < eligibleUsers.size(); i += groupSize) {
            groupCount++;
            String newGroupId = thisWeekStart.toString() + "-" + String.format("%03d", groupCount); // 예: 2025-08-25-001

            List<User> currentGroupMembers = new ArrayList<>();
            for (int j = 0; j < groupSize && (i + j) < eligibleUsers.size(); j++) {
                currentGroupMembers.add(eligibleUsers.get(i + j));
            }

            for (User member : currentGroupMembers) {
                member.setWeeklyRankingGroupId(newGroupId);
                // 스케줄러가 그룹을 배정하므로, 지난 주 랭킹은 여기서 초기화 (새로운 주 시작)
                member.setLastWeekRank(null);
            }
            userRepository.saveAll(currentGroupMembers);
            log.info("그룹 {} (ID: {}) 에 {}명 배정 완료.", groupCount, newGroupId, currentGroupMembers.size());
        }
        log.info("주간 랭킹 그룹 배정 스케줄러 종료. 총 {}개 그룹 생성.", groupCount);
    }
}