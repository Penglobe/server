package com.penglobe.server.service;

import com.penglobe.server.domain.ranking.AllRanking;
import com.penglobe.server.domain.ranking.WeeklyRanking;
import com.penglobe.server.domain.region.Regions;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.UserTotalScoreDTO;
import com.penglobe.server.dto.ranking.MyRankingDTO;
import com.penglobe.server.dto.ranking.RankingInfoDTO;
import com.penglobe.server.dto.ranking.WeeklyRankingResponseDTO;
import com.penglobe.server.dto.RegionDTO;
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
    private final AllRankingRepository allRankingRepository;
    private final TransportActivityRepository transportActivityRepository;
    private final DietRecordRepository dietRecordRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final RegionRepository regionRepository;

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

    /**
     * 전체 랭킹을 업데이트하는 로직
     */
    @Transactional
    public void updateAllRanking() {
        // 1. DB에서 직접 합산 및 정렬된 점수 목록 조회
        List<UserTotalScoreDTO> userScores = userCountersRepository.findUserTotalScores();

        // 2. 순위 부여 및 AllRanking 엔티티 생성
        List<AllRanking> allRankings = new ArrayList<>();
        for (int i = 0; i < userScores.size(); i++) {
            UserTotalScoreDTO current = userScores.get(i);

            // 점수가 0 이하인 사용자는 랭킹에서 제외
            if (current.getTotalScore() == null || current.getTotalScore().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            int rank;
            // 동점자 처리
            if (i > 0 && current.getTotalScore().compareTo(userScores.get(i - 1).getTotalScore()) == 0) {
                rank = allRankings.get(allRankings.size() - 1).getRanking();
            } else {
                rank = allRankings.size() + 1;
            }

            AllRanking rankingEntry = AllRanking.builder()
                    .userId(current.getUserId())
                    .nickname(current.getNickname())
                    .score(current.getTotalScore())
                    .ranking(rank)
                    .build();
            allRankings.add(rankingEntry);
        }

        // 3. 테이블 업데이트
        allRankingRepository.deleteAllInBatch();
        allRankingRepository.saveAll(allRankings);

        System.out.println("전체 랭킹 업데이트 완료. 처리된 사용자 수: " + allRankings.size());
    }

    /**
     * 지역별 랭킹 점수를 업데이트하는 로직
     */
    @Transactional
    public void updateRegionRankings() {
        List<Regions> allRegions = regionRepository.findAll();
        for (Regions region : allRegions) {
            BigDecimal totalCo2 = userCountersRepository.sumTotalCo2ByRegionId(region.getRegionId())
                    .orElse(BigDecimal.ZERO);
            region.setTotalCo2kg(totalCo2);
        }
        // @Transactional에 의해 메서드 종료 시 자동으로 DB에 업데이트됨
        System.out.println("지역별 랭킹 점수 업데이트 완료. 처리된 지역 수: " + allRegions.size());
    }

    /**
     * CO2 총 절감량 기준 지역별 랭킹을 조회합니다.
     * <p>
     * 1. Repository를 통해 각 지역의 CO2 절감량 합계를 내림차순으로 정렬하여 조회합니다.
     * 2. 조회된 리스트를 순회하며 순위를 부여합니다.
     * 3. 동점자 발생 시 같은 순위를 부여하고, 다음 순위는 동점자 수만큼 건너뛰어 계산합니다. (예: 1, 2, 2, 4)
     *
     * @return 순위가 포함된 RegionDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> getRegionRankings() {
        List<RegionDTO> regionRankings = regionRepository.getRegionRankings();
        int rank = 0;
        BigDecimal lastScore = new BigDecimal(-1); // 이전 점수를 저장할 변수

        for (int i = 0; i < regionRankings.size(); i++) {
            RegionDTO current = regionRankings.get(i);
            // 이전 점수와 다를 경우에만 순위를 i+1로 갱신
            if (current.getTotalCo2().compareTo(lastScore) != 0) {
                rank = i + 1;
            }
            current.setRank(rank);
            lastScore = current.getTotalCo2();
        }
        return regionRankings;
    }

    /**
     * 주간 랭킹 정보(Top 10 + 내 순위)를 조회하는 로직
     * @param currentUserId 현재 접속한 사용자의 ID
     * @return 주간 랭킹 응답 DTO
     */
    @Transactional(readOnly = true)
    public WeeklyRankingResponseDTO getWeeklyRanking(Long currentUserId) {
        // 1. Top 10 조회
        List<RankingInfoDTO> top10 = weeklyRankingRepository.findTop10ByOrderByRankingAsc().stream()
                .map(wr -> new RankingInfoDTO(wr.getRanking(), wr.getNickname(), wr.getScore()))
                .toList();

        // 2. 내 순위 조회
        MyRankingDTO myRank = weeklyRankingRepository.findByUserId(currentUserId)
                .map(wr -> new MyRankingDTO(wr.getRanking(), wr.getScore()))
                .orElse(null); // 랭킹에 없으면 null

        return new WeeklyRankingResponseDTO(top10, myRank);
    }

    /**
     * 전체 랭킹 정보(Top 10 + 내 순위)를 조회하는 로직
     * @param currentUserId 현재 접속한 사용자의 ID
     * @return 전체 랭킹 응답 DTO
     */
    @Transactional(readOnly = true)
    public WeeklyRankingResponseDTO getAllRanking(Long currentUserId) {
        // 1. Top 10 조회
        List<RankingInfoDTO> top10 = allRankingRepository.findTop10ByOrderByRankingAsc().stream()
                .map(ar -> new RankingInfoDTO(ar.getRanking(), ar.getNickname(), ar.getScore()))
                .toList();

        // 2. 내 순위 조회
        MyRankingDTO myRank = allRankingRepository.findByUserId(currentUserId)
                .map(ar -> new MyRankingDTO(ar.getRanking(), ar.getScore()))
                .orElse(null); // 랭킹에 없으면 null

        return new WeeklyRankingResponseDTO(top10, myRank);
    }
}
