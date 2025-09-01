package com.penglobe.server;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.PointDTO;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.service.PointLedgerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PointLedgerServiceUnitTest {

    @Mock
    private PointsLedgerRepository pointsLedgerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PointLedgerService pointLedgerService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 사용자
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setTotalPoint(100); // 최종 포인트
    }

    @Test
    public void testGetUserPointListWithBalanceUpdate() throws Exception {
        // 포인트 내역 더미 데이터
        PointsLedger ledger1 = new PointsLedger();
        ledger1.setUser(testUser);
        ledger1.setChangeAmount(50); // 적립
        ledger1.setReason(LedgerReason.QUIZ);

        PointsLedger ledger2 = new PointsLedger();
        ledger2.setUser(testUser);
        ledger2.setChangeAmount(-20); // 차감
        ledger2.setReason(LedgerReason.SHOP_PURCHASE);

        // Reflection으로 BaseEntity의 createdAt 세팅
        Field createdAtField = PointsLedger.class.getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(ledger1, LocalDateTime.of(2025, 9, 1, 10, 0));
        createdAtField.set(ledger2, LocalDateTime.of(2025, 9, 1, 11, 0));

        // Mock 동작 설정
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(pointsLedgerRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(ledger2, ledger1)); // 최신 순

        // 서비스 호출
        List<PointDTO> result = pointLedgerService.getUserPointList(1L);
        int runningBalance = testUser.getTotalPoint();
        for (PointDTO dto : result) {
            dto.setBalanceAfter(runningBalance + dto.getChangeAmount());
            runningBalance += dto.getChangeAmount();
        }



        // 출력
        System.out.println("==== 포인트 내역 ====");
        System.out.println("현재 보유 포인트량: " + testUser.getTotalPoint());
        result.forEach(dto -> {
            System.out.println("변경량: " + dto.getChangeAmount() +
                    ", 사유: " + dto.getReason() +
                    ", 총 포인트: " + dto.getBalanceAfter() +
                    ", 생성일: " + dto.getCreatedAt());
        });

        //BaseEntity에 createdAt 필드가 있고, 보통 엔티티가 DB에 저장될 때
        //JPA가 자동으로 @PrePersist로 값을 넣어주도록 설정되어 있다면 실제 DB에서는 값이 들어가지만
        //단위 테스트에서는 DB에 저장하지 않고 그냥 객체를 만들어서 반환하기 때문에 자동으로 값이 채워지지 않아요.
    }
}
