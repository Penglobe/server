package com.penglobe.server;

import com.penglobe.server.domain.ranking.AllRanking;
import com.penglobe.server.domain.ranking.WeeklyRanking;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.ranking.MyRankingDTO;
import com.penglobe.server.dto.ranking.WeeklyRankingResponseDTO;
import com.penglobe.server.repository.*;
import com.penglobe.server.service.RankingService;
import com.penglobe.server.service.MyPageService;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.diet.DietRecord;
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
import java.util.ArrayList;
import com.penglobe.server.domain.region.Regions;

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

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCountersRepository userCountersRepository;
    @Mock
    private WeeklyRankingRepository weeklyRankingRepository;
    @Mock
    private AllRankingRepository allRankingRepository;
    @Mock
    private TransportActivityRepository transportActivityRepository;
    @Mock
    private DietRecordRepository dietRecordRepository;
    @Mock
    private SurveyResponseRepository surveyResponseRepository;
    @Mock
    private RegionRepository regionRepository;

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

    @Test
    @DisplayName("전체 랭킹 조회 시, Top10과 함께 내 순위가 정확히 조회되어야 한다")
    void getAllRanking_WhenCurrentUserIsOutsideTop10() {
        // Given
        long currentUserId = 12L;
        String currentUsername = "user" + currentUserId;

        // 1. 15명의 전체 랭킹 데이터 모의 생성
        List<AllRanking> mockRankings = LongStream.rangeClosed(1, 15)
                .mapToObj(i -> AllRanking.builder()
                        .userId(i)
                        .nickname("user" + i)
                        .ranking((int) i)
                        .score(BigDecimal.valueOf(2000 - (i * 100)))
                        .build())
                .toList();

        List<AllRanking> top10 = mockRankings.subList(0, 10);
        AllRanking currentUserRanking = mockRankings.get(11); // 12번째 사용자

        when(allRankingRepository.findTop10ByOrderByRankingAsc()).thenReturn(top10);
        when(allRankingRepository.findByUserId(currentUserId)).thenReturn(Optional.of(currentUserRanking));

        // When
        System.out.println(String.format("\n--- 현재 접속자: %s (전체 랭킹 15명 중 12위) ---", currentUsername));
        WeeklyRankingResponseDTO response = rankingService.getAllRanking(currentUserId);

        // Then & Print
        assertThat(response).isNotNull();
        assertThat(response.getTop10()).hasSize(10);
        assertThat(response.getMyRank()).isNotNull();
        assertThat(response.getMyRank().getRank()).isEqualTo(12);

        System.out.println("[전체 랭킹 Top 10]");
        response.getTop10().forEach(ranking ->
                System.out.printf("순위: %-2d, 닉네임: %-7s, 점수: %s%n",
                        ranking.getRank(),
                        ranking.getNickname(),
                        ranking.getScore().toPlainString()));

        System.out.println("\n[My All-Time Rank]");
        MyRankingDTO myRank = response.getMyRank();
        System.out.printf("순위: %-2d, 점수: %s%n",
                myRank.getRank(),
                myRank.getScore().toPlainString());
        System.out.println("------------------------------------\n");
    }

    @Test
    @DisplayName("지역별 랭킹 점수 업데이트가 정확히 계산되어 저장되어야 한다")
    void updateRegionRankings_Success() {
        // Given
        // 1. 17개 지역 데이터 모의 생성
        List<String> regionNames = List.of(
                "서울특별시", "경기도", "부산광역시", "대구광역시", "인천광역시",
                "광주광역시", "대전광역시", "울산광역시", "세종특별자치시", "강원특별자치도",
                "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
                "경상남도", "제주특별자치도"
        );

        List<Regions> mockRegions = new ArrayList<>();
        for (int i = 0; i < regionNames.size(); i++) {
            mockRegions.add(Regions.builder().regionId(i + 1).name(regionNames.get(i)).build());
        }

        when(regionRepository.findAll()).thenReturn(mockRegions);

        // 2. 각 지역별로 예상 점수 설정
        for (Regions region : mockRegions) {
            // 예: 서울(1) = 1700점, 경기(2)=1600점 ... 제주(17)=100점
            BigDecimal expectedScore = BigDecimal.valueOf(100L * (18 - region.getRegionId()));
            when(userCountersRepository.sumTotalCo2ByRegionId(region.getRegionId()))
                    .thenReturn(Optional.of(expectedScore));
        }

        // When
        rankingService.updateRegionRankings();

        // Then
        // 각 Region 객체의 totalCo2 필드가 예상 점수로 업데이트되었는지 확인
        System.out.println("\n--- 지역별 랭킹 점수 계산 테스트 결과 ---");
        for (int i = 0; i < mockRegions.size(); i++) {
            Regions region = mockRegions.get(i);
            BigDecimal expectedScore = BigDecimal.valueOf(100L * (18 - region.getRegionId()));
            assertThat(region.getTotalCo2kg()).isEqualByComparingTo(expectedScore);

            System.out.printf("지역: %-10s, 계산된 점수: %s%n", region.getName(), region.getTotalCo2kg());
        }
        System.out.println("------------------------------------\n");

        // 서비스가 끝난 후, region 객체들의 점수가 null이 아닌지 추가 확인
        assertThat(mockRegions.get(0).getTotalCo2kg()).isNotNull();
        assertThat(mockRegions.get(16).getTotalCo2kg()).isNotNull();
    }

    @Test
    @DisplayName("사용자별 점수 데이터를 기반으로 지역별 총점이 올바르게 계산되어 저장되어야 한다")
    void updateRegionRankings_WithActualUserData() {
        // Given
        // 1. 지역 및 사용자, 사용자별 점수 데이터 모의 생성
        Regions seoul = Regions.builder().regionId(1).name("서울특별시").build();
        Regions gyeonggi = Regions.builder().regionId(2).name("경기도").build();
        List<Regions> mockRegions = List.of(seoul, gyeonggi);

        // 서울 사용자 3명 (10+20+30 = 60점)
        User user1 = User.builder().userId(1L).regionId(1).build();
        UserCounters counters1 = UserCounters.builder().user(user1).totalDistanceCo2Kg(BigDecimal.valueOf(10)).totalDietCo2Kg(BigDecimal.ZERO).totalSurveyCo2Kg(BigDecimal.ZERO).build();

        User user2 = User.builder().userId(2L).regionId(1).build();
        UserCounters counters2 = UserCounters.builder().user(user2).totalDistanceCo2Kg(BigDecimal.valueOf(20)).totalDietCo2Kg(BigDecimal.ZERO).totalSurveyCo2Kg(BigDecimal.ZERO).build();

        User user3 = User.builder().userId(3L).regionId(1).build();
        UserCounters counters3 = UserCounters.builder().user(user3).totalDistanceCo2Kg(BigDecimal.valueOf(30)).totalDietCo2Kg(BigDecimal.ZERO).totalSurveyCo2Kg(BigDecimal.ZERO).build();

        // 경기 사용자 2명 (40+50 = 90점)
        User user4 = User.builder().userId(4L).regionId(2).build();
        UserCounters counters4 = UserCounters.builder().user(user4).totalDistanceCo2Kg(BigDecimal.valueOf(40)).totalDietCo2Kg(BigDecimal.ZERO).totalSurveyCo2Kg(BigDecimal.ZERO).build();

        User user5 = User.builder().userId(5L).regionId(2).build();
        UserCounters counters5 = UserCounters.builder().user(user5).totalDistanceCo2Kg(BigDecimal.valueOf(50)).totalDietCo2Kg(BigDecimal.ZERO).totalSurveyCo2Kg(BigDecimal.ZERO).build();

        // 2. Mock 설정
        when(regionRepository.findAll()).thenReturn(mockRegions);

        // 실제라면 DB 쿼리가 이 계산을 수행할 것이라고 가정하고, 테스트에서는 수동으로 계산한 값을 반환하도록 설정
        BigDecimal seoulScore = counters1.getTotalDistanceCo2Kg().add(counters2.getTotalDistanceCo2Kg()).add(counters3.getTotalDistanceCo2Kg());
        BigDecimal gyeonggiScore = counters4.getTotalDistanceCo2Kg().add(counters5.getTotalDistanceCo2Kg());

        when(userCountersRepository.sumTotalCo2ByRegionId(1)).thenReturn(Optional.of(seoulScore));
        when(userCountersRepository.sumTotalCo2ByRegionId(2)).thenReturn(Optional.of(gyeonggiScore));

        // When
        rankingService.updateRegionRankings();

        // Then
        System.out.println("\n--- 사용자 데이터 기반 지역 점수 계산 테스트 ---");
        System.out.printf("서울 예상 점수: %s, 실제 저장된 점수: %s%n", seoulScore, seoul.getTotalCo2kg());
        System.out.printf("경기 예상 점수: %s, 실제 저장된 점수: %s%n", gyeonggiScore, gyeonggi.getTotalCo2kg());
        System.out.println("------------------------------------------\n");

        assertThat(seoul.getTotalCo2kg()).isEqualByComparingTo(seoulScore);
        assertThat(gyeonggi.getTotalCo2kg()).isEqualByComparingTo(gyeonggiScore);
    }

    @Test
    @DisplayName("마이페이지 기능: 사용자 정보 및 일별 탄소 절감량 조회가 정확해야 한다")
    void testMyPageFunctionality() {
        // Given
        Long userId = 100L;
        LocalDate testDate = LocalDate.of(2025, 9, 1);

        // 1. User Mock Data
        User user = User.builder()
                .userId(userId)
                .nickname("테스트유저")
                .totalPoint(500)
                .build();
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        // 2. UserCounters Mock Data
        UserCounters userCounters = UserCounters.builder()
                .userId(userId)
                .attendanceTotalDays(30)
                .longestAttendanceStreak(15)
                .attendanceStreakDays(7) // Add this line
                .build();
        when(userCountersRepository.findByUserId(userId)).thenReturn(Optional.of(userCounters));

        // 3. TransportActivity Mock Data
        TransportActivity transportActivity = TransportActivity.builder()
                .user(user)
                .co2Kg(BigDecimal.valueOf(0.50))
                .build();
        when(transportActivityRepository.findByUserUserIdAndActivityDate(userId, testDate))
                .thenReturn(List.of(transportActivity));

        // 4. DietRecord Mock Data
        DietRecord dietRecord = DietRecord.builder()
                .user(user)
                .co2Kg(1) // Integer type
                .build();
        when(dietRecordRepository.findByUserUserIdAndRecordDate(userId, testDate))
                .thenReturn(List.of(dietRecord));

        // When
        MyPageDTO myPageInfo = myPageService.getMyPageInfo(userId);
        DailyCarbonReductionDTO dailyReduction = myPageService.getDailyCarbonReduction(userId, testDate);

        // Then - MyPageInfo Verification
        assertThat(myPageInfo).isNotNull();
        assertThat(myPageInfo.getNickname()).isEqualTo("테스트유저");
        assertThat(myPageInfo.getTotalPoint()).isEqualTo(500);
        assertThat(myPageInfo.getAttendanceTotalDays()).isEqualTo(30);
        assertThat(myPageInfo.getLongestAttendanceStreak()).isEqualTo(15);

        // Then - DailyCarbonReduction Verification
        assertThat(dailyReduction).isNotNull();
        assertThat(dailyReduction.getTransportCo2Kg()).isEqualByComparingTo(BigDecimal.valueOf(0.50));
        assertThat(dailyReduction.getDietCo2Kg()).isEqualByComparingTo(BigDecimal.valueOf(1.00)); // Integer 1 converted to BigDecimal 1.00
        assertThat(dailyReduction.getTotalCo2Kg()).isEqualByComparingTo(BigDecimal.valueOf(1.50));

        System.out.println(" --- 마이페이지 기능 테스트 결과 ---");
                System.out.printf("닉네임: %s, 보유 포인트: %d, 누적 출석: %d일, 최장 연속 출석: %d일, 현재 연속 출석: %d일%n",
                myPageInfo.getNickname(), myPageInfo.getTotalPoint(),
                myPageInfo.getAttendanceTotalDays(), myPageInfo.getLongestAttendanceStreak(), myPageInfo.getAttendanceStreakDays());
        System.out.printf("2025-09-01 환경걸음 절감량: %s kg, 식단 절감량: %s kg, 총 절감량: %s kg%n",
                dailyReduction.getTransportCo2Kg(), dailyReduction.getDietCo2Kg(), dailyReduction.getTotalCo2Kg());
        System.out.println("----------------------------------- ");
    }
}
