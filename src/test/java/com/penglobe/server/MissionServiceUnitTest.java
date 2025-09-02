package com.penglobe.server;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.mission.MissionClaim;
import com.penglobe.server.domain.mission.MissionDefinition;
import com.penglobe.server.domain.mission.MissionMetric;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.MissionSlotDTO;
import com.penglobe.server.repository.*;
import com.penglobe.server.service.MissionService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.Mockito.*;

class MissionServiceUnitTest {

    @Mock private MissionDefinitionRepository defRepo;
    @Mock private MissionClaimRepository claimRepo;
    @Mock private UserCountersRepository countersRepo;
    @Mock private UserRepository userRepo;
    @Mock private PointsLedgerRepository pointsLedgerRepo;

    @InjectMocks private MissionService missionService;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("걸음 미션 getWindow 테스트 (콘솔 출력)")
    void getWindow_walkMission_prints() {
        // given
        Long userId = 1L;
        MissionMetric metric = MissionMetric.WALK_CO2_KG;

        UserCounters counters = new UserCounters();
        counters.setUserId(userId);
        counters.setTotalDistanceCo2Kg(BigDecimal.valueOf(25)); // 25kg 진행

        MissionDefinition def = MissionDefinition.builder()
                .metric(metric)
                .startTarget(10L)
                .step(10L)
                .build();

        when(countersRepo.findById(userId)).thenReturn(Optional.of(counters));
        when(defRepo.findByMetric(metric)).thenReturn(def);
        when(claimRepo.findMaxClaimedTarget(userId, metric)).thenReturn(null);
        when(claimRepo.findByUserIdAndMetric(userId, metric)).thenReturn(List.of());

        // when
        var result = missionService.getWindow(userId, metric);

        // then
        System.out.println("==== 걸음 미션 윈도우 ====");
        result.forEach(slot -> {
            System.out.printf("target=%d, progress=%s, reward=%d, claimable=%s, claimed=%s%n",
                    slot.getTarget(), slot.getProgress(), slot.getRewardPoints(),
                    slot.isClaimable(), slot.isClaimed());
        });

        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("출석 미션 claim 성공 테스트 (콘솔 출력)")
    void claim_attendance_success_prints() {
        // given
        Long userId = 2L;
        MissionMetric metric = MissionMetric.ATTEND_MONTH_DAYS;

        UserCounters counters = new UserCounters();
        counters.setUserId(userId);
        counters.setAttendanceMonthDays(25); // 25일 출석

        User user = User.builder()
                .userId(userId)
                .totalPoint(0)
                .build();

        when(countersRepo.findById(userId)).thenReturn(Optional.of(counters));
        when(claimRepo.existsByUserIdAndMetricAndPeriodMonth(eq(userId), eq(metric), anyString()))
                .thenReturn(false);
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(claimRepo.save(any(MissionClaim.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pointsLedgerRepo.save(any(PointsLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        missionService.claim(userId, metric, 20L);

        // then
        System.out.println("==== 출석 미션 수령 ====");
        System.out.println("userId: " + user.getUserId());
        System.out.println("총 포인트: " + user.getTotalPoint());

        verify(pointsLedgerRepo).save(argThat(pl -> pl.getReason() == LedgerReason.MISSION_REWARD));
        Assertions.assertTrue(user.getTotalPoint() > 0);
    }

    @Test
    @DisplayName("걸음 미션 claim 실패 테스트 (진행도 부족, 콘솔 출력)")
    void claim_walkMission_fail_prints() {
        // given
        Long userId = 3L;
        MissionMetric metric = MissionMetric.WALK_CO2_KG;

        UserCounters counters = new UserCounters();
        counters.setUserId(userId);
        counters.setTotalDistanceCo2Kg(BigDecimal.valueOf(5)); // 5kg → 목표 미달

        MissionDefinition def = MissionDefinition.builder()
                .metric(metric)
                .startTarget(10L)
                .step(10L)
                .build();

        when(countersRepo.findById(userId)).thenReturn(Optional.of(counters));
        when(defRepo.findByMetric(metric)).thenReturn(def);

        try {
            missionService.claim(userId, metric, 10L);
            Assertions.fail("예외가 발생해야 합니다.");
        } catch (IllegalArgumentException ex) {
            System.out.println("==== 걸음 미션 수령 실패 ====");
            System.out.println("메시지: " + ex.getMessage());
            Assertions.assertTrue(ex.getMessage().contains("아직 목표치에 도달"));
        }
    }
}
