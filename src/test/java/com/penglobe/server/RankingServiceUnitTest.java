package com.penglobe.server;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.ranking.MyRankingDTO;
import com.penglobe.server.dto.ranking.WeeklyRankingResponseDTO;
import com.penglobe.server.repository.*;
import com.penglobe.server.service.RankingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingServiceUnitTest {

    @InjectMocks
    private RankingService rankingService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCountersRepository userCountersRepository;
    @Mock
    private WeeklyRankingRepository weeklyRankingRepository;
    @Mock
    private TransportActivityRepository transportActivityRepository;
    @Mock
    private DietRecordRepository dietRecordRepository;
    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Test
    @DisplayName("주간 랭킹 업데이트 시, 동점자 처리 및 전체 결과가 정확해야 한다")
    void updateLiveWeeklyRanking_Success() {
        // Given (준비)
        // 1. 15명의 사용자 데이터 생성
        List<User> allUsers = LongStream.rangeClosed(1, 15)
                .mapToObj(id -> User.builder().userId(id).nickname("user" + id).build())
                .toList();

        // 2. 10명은 활성, 5명은 비활성으로 설정
        List<User> activeUsers = allUsers.subList(0, 10);
        when(userCountersRepository.findUsersActiveSince(any(LocalDate.class))).thenReturn(activeUsers);

        // 3. 각 활성 사용자별로 점수 설정 (user3, user4 동점자 생성)
        for (int i = 0; i < activeUsers.size(); i++) {
            User user = activeUsers.get(i);
            long scoreMultiplier = 10 - i;

            // user4(index=3)가 user3(index=2)과 같은 점수를 갖도록 설정
            if (i == 3) {
                scoreMultiplier = 10 - 2; // user3의 점수 배수
            }

            when(transportActivityRepository.sumCo2KgByUserIdAndCreatedAtBetween(eq(user.getUserId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Optional.of(BigDecimal.valueOf(10 * scoreMultiplier)));
            when(dietRecordRepository.sumCo2KgByUserAndPeriod(eq(user.getUserId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(BigDecimal.valueOf(5 * scoreMultiplier));
            when(surveyResponseRepository.sumTotalCo2KgByUserAndPeriod(eq(user.getUserId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(BigDecimal.valueOf(2 * scoreMultiplier));
        }

        // When (실행)
        rankingService.updateLiveWeeklyRanking();

        // Then (검증)
        ArgumentCaptor<List<WeeklyRanking>> captor = ArgumentCaptor.forClass(List.class);
        verify(weeklyRankingRepository).saveAll(captor.capture());
        List<WeeklyRanking> savedRankings = captor.getValue();

        // [콘솔 출력] 랭킹 결과 확인
        System.out.println("\n--- 주간 랭킹 테스트 결과 ---");
        savedRankings.forEach(ranking ->
                System.out.printf("순위: %-2d, 닉네임: %-7s, 점수: %s%n",
                        ranking.getRanking(),
                        ranking.getNickname(),
                        ranking.getScore().toPlainString()));
        System.out.println("--------------------------\n");

        // 검증 로직
        assertThat(savedRankings).hasSize(10);
        // 1, 2위 확인
        assertThat(savedRankings.get(0).getRanking()).isEqualTo(1);
        assertThat(savedRankings.get(1).getRanking()).isEqualTo(2);
        // 동점자(공동 3위) 확인
        assertThat(savedRankings.get(2).getRanking()).isEqualTo(3);
        assertThat(savedRankings.get(3).getRanking()).isEqualTo(3);
        assertThat(savedRankings.get(2).getScore()).isEqualByComparingTo(savedRankings.get(3).getScore());
        // 동점자 다음 순위(5위) 확인
        assertThat(savedRankings.get(4).getRanking()).isEqualTo(5);
    }

    @Test
    @DisplayName("주간 랭킹 마감 시, 최종 랭킹이 User에 기록되고 실시간 랭킹은 삭제되어야 한다")
    void finalizeWeeklyRanking_Success() {
        // Given
        // 1. 최종 랭킹 데이터 생성 (3명)
        User user1 = User.builder().userId(1L).nickname("winner").build();
        User user2 = User.builder().userId(2L).nickname("runner-up").build();
        User user3 = User.builder().userId(3L).nickname("third-place").build();

        List<WeeklyRanking> finalRankings = List.of(
                WeeklyRanking.builder().userId(1L).nickname("winner").ranking(1).score(BigDecimal.valueOf(100)).build(),
                WeeklyRanking.builder().userId(2L).nickname("runner-up").ranking(2).score(BigDecimal.valueOf(90)).build(),
                WeeklyRanking.builder().userId(3L).nickname("third-place").ranking(3).score(BigDecimal.valueOf(80)).build()
        );
        when(weeklyRankingRepository.findAll()).thenReturn(finalRankings);

        List<User> rankedUsers = List.of(user1, user2, user3);
        when(userRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(rankedUsers);

        // When
        rankingService.finalizeWeeklyRanking();

        // Then
        // 1. 랭킹 초기화 메서드가 호출되었는지 확인
        verify(userRepository).resetAllLastWeekRanks();

        // 2. 각 User 객체의 lastWeekRank가 올바르게 설정되었는지 확인
        assertThat(user1.getLastWeekRank()).isEqualTo(1);
        assertThat(user2.getLastWeekRank()).isEqualTo(2);
        assertThat(user3.getLastWeekRank()).isEqualTo(3);

        // 3. 실시간 랭킹 테이블을 비우는 메서드가 호출되었는지 확인
        verify(weeklyRankingRepository).deleteAllInBatch();
    }

    @Test
    @DisplayName("현재 사용자가 11위일 때, Top10과 함께 내 순위가 정확히 조회되어야 한다")
    void getWeeklyRanking_WhenCurrentUserIsOutsideTop10() {
        // Given
        long currentUserId = 11L;
        String currentUsername = "user" + currentUserId;

        // 1. 15명의 랭킹 데이터 모의 생성
        List<WeeklyRanking> mockRankings = LongStream.rangeClosed(1, 15)
                .mapToObj(i -> WeeklyRanking.builder()
                        .userId(i)
                        .nickname("user" + i)
                        .ranking((int) i)
                        .score(BigDecimal.valueOf(100 - (i * 5)))
                        .build())
                .toList();

        List<WeeklyRanking> top10 = mockRankings.subList(0, 10);
        WeeklyRanking currentUserRanking = mockRankings.get(10); // 11번째 사용자

        when(weeklyRankingRepository.findTop10ByOrderByRankingAsc()).thenReturn(top10);
        when(weeklyRankingRepository.findByUserId(currentUserId)).thenReturn(Optional.of(currentUserRanking));

        // When
        System.out.println(String.format("\n--- 현재 접속자: %s (15명 중 11위) ---", currentUsername));
        WeeklyRankingResponseDTO response = rankingService.getWeeklyRanking(currentUserId);

        // Then & Print
        assertThat(response).isNotNull();
        assertThat(response.getTop10()).hasSize(10);
        assertThat(response.getMyRank()).isNotNull();
        assertThat(response.getMyRank().getRank()).isEqualTo(11);

        System.out.println("[Top 10]");
        response.getTop10().forEach(ranking ->
                System.out.printf("순위: %-2d, 닉네임: %-7s, 점수: %s%n",
                        ranking.getRank(),
                        ranking.getNickname(),
                        ranking.getScore().toPlainString()));

        System.out.println("\n[My Rank]");
        MyRankingDTO myRank = response.getMyRank();
        System.out.printf("순위: %-2d, 점수: %s%n",
                myRank.getRank(),
                myRank.getScore().toPlainString());
        System.out.println("------------------------------------\n");
    }
}
