package com.penglobe.server.service;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;
    private final WeeklyRankingRepository weeklyRankingRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;
    private final SurveyResponseRepository surveyResponseRepository;

    // 사용자 점수를 임시로 저장하기 위한 내부 record
    private record UserScore(User user, BigDecimal score) {}

    /**
     * 실시간 주간 랭킹을 업데이트하는 로직
     */
    @Transactional
    public void updateLiveWeeklyRanking() {
        // 1. 날짜 범위 정의
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDate sevenDaysAgo = now.toLocalDate().minusDays(7);

        // 2. 랭킹 대상 사용자 조회
        List<User> activeUsers = userCountersRepository.findUsersActiveSince(sevenDaysAgo);

        // 3. 사용자별 점수 계산
        List<UserScore> userScores = new ArrayList<>();
        for (User user : activeUsers) {
            BigDecimal transportScore = transportActivityRepository.sumCo2KgByUserIdAndCreatedAtBetween(user.getUserId(), startOfWeek, now).orElse(BigDecimal.ZERO);
            BigDecimal dietScore = dietRecordRepository.sumCo2KgByUserAndPeriod(user.getUserId(), startOfWeek, now);
            BigDecimal surveyScore = surveyResponseRepository.sumTotalCo2KgByUserAndPeriod(user.getUserId(), startOfWeek, now);

            BigDecimal totalScore = transportScore.add(dietScore).add(surveyScore);

            // 절감량이 0보다 큰 사용자만 랭킹에 포함
            if (totalScore.compareTo(BigDecimal.ZERO) > 0) {
                userScores.add(new UserScore(user, totalScore));
            }
        }

        // 4. 점수 기준으로 내림차순 정렬
        userScores.sort(Comparator.comparing(UserScore::score).reversed());

        // 5. 순위 부여 및 WeeklyRanking 엔티티 생성
        List<WeeklyRanking> weeklyRankings = new ArrayList<>();
        int rank = 0;
        for (int i = 0; i < userScores.size(); i++) {
            UserScore current = userScores.get(i);
            // 동점자 처리: 이전 사용자와 점수가 같으면 같은 순위 부여
            if (i > 0 && current.score().compareTo(userScores.get(i - 1).score()) == 0) {
                rank = weeklyRankings.get(i - 1).getRanking();
            } else {
                rank = i + 1;
            }

            WeeklyRanking rankingEntry = WeeklyRanking.builder()
                    .userId(current.user().getUserId())
                    .nickname(current.user().getNickname())
                    .score(current.score())
                    .ranking(rank)
                    .build();
            weeklyRankings.add(rankingEntry);
        }

        // 6. 테이블 업데이트
        weeklyRankingRepository.deleteAllInBatch();
        weeklyRankingRepository.saveAll(weeklyRankings);

        System.out.println("실시간 랭킹 업데이트 완료. 처리된 사용자 수: " + weeklyRankings.size());
    }

    /**
     * 주간 랭킹을 마감하고 Users 테이블에 순위를 기록하는 로직
     */
    @Transactional
    public void finalizeWeeklyRanking() {
        // 1. 모든 사용자의 지난주 랭킹 초기화
        userRepository.resetAllLastWeekRanks();

        // 2. 최종 랭킹 데이터 조회
        List<WeeklyRanking> finalRankings = weeklyRankingRepository.findAll();
        if (finalRankings.isEmpty()) {
            System.out.println("주간 랭킹 마감: 처리할 랭킹 데이터가 없습니다.");
            return;
        }

        // 3. User 엔티티에 최종 랭킹 기록 (효율적인 방식)
        List<Long> userIds = finalRankings.stream()
                .map(WeeklyRanking::getUserId)
                .toList();
        List<User> usersToUpdate = userRepository.findAllById(userIds);

        Map<Long, User> userMap = usersToUpdate.stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        for (WeeklyRanking ranking : finalRankings) {
            User user = userMap.get(ranking.getUserId());
            if (user != null) {
                user.setLastWeekRank(ranking.getRanking());
            }
        }
        // userRepository.saveAll(usersToUpdate); // @Transactional에 의해 자동 저장

        // 4. 이번 주를 위해 실시간 랭킹 테이블 비우기
        weeklyRankingRepository.deleteAllInBatch();

        System.out.println("주간 랭킹 마감 완료. 처리된 사용자 수: " + usersToUpdate.size());
    }
}
