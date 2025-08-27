package com.penglobe.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.PersonalRankingResponseDTO;
import com.penglobe.server.dto.RankedUserDTO;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.TransportActivityRepository;
import com.penglobe.server.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.math.BigDecimal; // Added import

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
class RankingServiceTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransportActivityRepository transportActivityRepository;

    @MockBean
    private DietRecordRepository dietRecordRepository;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private ObjectMapper objectMapper; // JSON 출력을 위해 ObjectMapper 주입

    

    @Test
    @DisplayName("동점자가 있을 경우 순위를 올바르게 부여한다 (스케줄러 배정 그룹)")
    void getPersonalWeeklyRanking_withTies_shouldAssignRanksCorrectly() throws Exception {
        // given
        Long currentUserId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.with(DayOfWeek.MONDAY);
        String testGroupId = thisWeekStart.toString() + "-001"; // 스케줄러가 배정한 그룹 ID 형식

        // 4명의 유저 생성 (스케줄러에 의해 이미 그룹이 배정되었다고 가정)
        User currentUser = User.builder().id(currentUserId).nickname("CurrentUser").weeklyRankingGroupId(testGroupId).build();
        User user2 = User.builder().id(2L).nickname("User2").weeklyRankingGroupId(testGroupId).build();
        User user3 = User.builder().id(3L).nickname("User3").weeklyRankingGroupId(testGroupId).build();
        User user4 = User.builder().id(4L).nickname("User4").weeklyRankingGroupId(testGroupId).build();

        List<User> group = Arrays.asList(currentUser, user2, user3, user4);

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(currentUser));
        when(userRepository.findByWeeklyRankingGroupId(testGroupId)).thenReturn(group);

        // 각 유저별 점수 설정 (동점자 발생)
        // 1위: currentUser (100점)
        // 공동 2위: user2, user3 (90점)
        // 4위: user4 (80점)
        when(transportActivityRepository.sumCo2KgByUserIdAndTakenOnBetween(1L, thisWeekStart, today.plusDays(1))).thenReturn(Optional.of(100L));
        when(transportActivityRepository.sumCo2KgByUserIdAndTakenOnBetween(2L, thisWeekStart, today.plusDays(1))).thenReturn(Optional.of(90L));
        when(transportActivityRepository.sumCo2KgByUserIdAndTakenOnBetween(3L, thisWeekStart, today.plusDays(1))).thenReturn(Optional.of(90L));
        when(transportActivityRepository.sumCo2KgByUserIdAndTakenOnBetween(4L, thisWeekStart, today.plusDays(1))).thenReturn(Optional.of(80L));
        when(dietRecordRepository.sumCo2KgByUserIdAndTakenOnBetween(anyLong(), any(), any())).thenReturn(Optional.of(0L));

        // when
        PersonalRankingResponseDTO response = rankingService.getPersonalWeeklyRanking(currentUserId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCurrentRanking()).hasSize(4);

        List<RankedUserDTO> ranking = response.getCurrentRanking();

        // 순위 검증
        assertThat(ranking.get(0).getUserId()).isEqualTo(1L);
        assertThat(ranking.get(0).getRank()).isEqualTo(1);

        // 동점자 순위 검증 (user2, user3는 2위)
        assertThat(ranking.get(1).getRank()).isEqualTo(2);
        assertThat(ranking.get(2).getRank()).isEqualTo(2);
        // user2와 user3의 순서는 점수가 같으므로 보장되지 않음, 두명의 유저 아이디가 2,3인지 확인
        Set<Long> tiedUserIds = Set.of(ranking.get(1).getUserId(), ranking.get(2).getUserId());
        assertThat(tiedUserIds).containsExactlyInAnyOrder(2L, 3L);


        assertThat(ranking.get(3).getUserId()).isEqualTo(4L);
        assertThat(ranking.get(3).getRank()).isEqualTo(4);

        // --- 결과 확인을 위한 콘솔 출력 ---
        System.out.println("\n--- TIE TEST RESULT DTO ---");
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(response));
        System.out.println("---------------------------\n");
    }

    @Test
    @DisplayName("전체 랭킹 조회 시 순위를 올바르게 부여한다")
    void getOverallRanking_shouldAssignRanksCorrectly() throws Exception {
        // given
        // Mock raw data from UserRepository.findAllUsersWithTotalSavingsForRanking()
        List<RankedUserDTO> mockRankedUsers = new ArrayList<>();
        mockRankedUsers.add(new RankedUserDTO(1L, "UserA", null, BigDecimal.valueOf(100), null)); // User 1, 100 saving
        mockRankedUsers.add(new RankedUserDTO(2L, "UserB", null, BigDecimal.valueOf(90), null));  // User 2, 90 saving
        mockRankedUsers.add(new RankedUserDTO(3L, "UserC", null, BigDecimal.valueOf(90), null));  // User 3, 90 saving (tie with UserB)
        mockRankedUsers.add(new RankedUserDTO(4L, "UserD", null, BigDecimal.valueOf(80), null));  // User 4, 80 saving

        // Mock the UserRepository method to return the raw data
        when(userRepository.findAllUsersWithTotalSavingsForRanking()).thenReturn(mockRankedUsers);

        // when
        List<RankedUserDTO> overallRanking = rankingService.getOverallRanking();

        // then
        assertThat(overallRanking).isNotNull();
        assertThat(overallRanking).hasSize(4);

        // Verify ranks and data
        assertThat(overallRanking.get(0).getUserId()).isEqualTo(1L);
        assertThat(overallRanking.get(0).getNickname()).isEqualTo("UserA");
        assertThat(overallRanking.get(0).getRank()).isEqualTo(1);
        assertThat(overallRanking.get(0).getTotalCo2Saved()).isEqualTo(BigDecimal.valueOf(100));

        assertThat(overallRanking.get(1).getUserId()).isEqualTo(2L);
        assertThat(overallRanking.get(1).getNickname()).isEqualTo("UserB");
        assertThat(overallRanking.get(1).getRank()).isEqualTo(2);
        assertThat(overallRanking.get(1).getTotalCo2Saved()).isEqualTo(BigDecimal.valueOf(90));

        assertThat(overallRanking.get(2).getUserId()).isEqualTo(3L);
        assertThat(overallRanking.get(2).getNickname()).isEqualTo("UserC");
        assertThat(overallRanking.get(2).getRank()).isEqualTo(2); // Corrected typo
        assertThat(overallRanking.get(2).getTotalCo2Saved()).isEqualTo(BigDecimal.valueOf(90));

        assertThat(overallRanking.get(3).getUserId()).isEqualTo(4L);
        assertThat(overallRanking.get(3).getNickname()).isEqualTo("UserD");
        assertThat(overallRanking.get(3).getRank()).isEqualTo(4);
        assertThat(overallRanking.get(3).getTotalCo2Saved()).isEqualTo(BigDecimal.valueOf(80));

        // --- 결과 확인을 위한 콘솔 출력 ---
        System.out.println("\n--- OVERALL RANKING TEST RESULT DTO ---");
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(overallRanking));
        System.out.println("---------------------------------------\n");
    }
}
