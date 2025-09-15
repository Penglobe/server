// src/main/java/com/penglobe/server/service/PaymentService.java
package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.Payment;
import com.penglobe.server.domain.user.PaymentStatus;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.PaymentRepository; // ✅ import 추가
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 결제 시작 전, 결제 기록을 생성하고 merchant_uid를 반환합니다.
     * @param userId 결제 사용자 ID
     * @param amount 결제 금액
     * @return 생성된 결제 기록의 merchantUid
     */
    @Transactional
    public String preparePayment(Long userId, Integer amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 고유한 merchantUid 생성
        String merchantUid = "mid_" + System.currentTimeMillis();

        Payment payment = Payment.builder()
                .merchantUid(merchantUid)
                .user(user)
                .amount(amount)
                .status(PaymentStatus.PENDING) // 초기 상태는 '대기'
                .build();

        paymentRepository.save(payment);
        return merchantUid;
    }

    /**
     * 결제 검증 완료 후, 사용자 포인트를 충전하고 내역을 저장합니다.
     * @param merchantUid 결제 기록 조회용 고유 주문번호
     * @param amount 충전할 금액 (결제 금액)
     */
    @Transactional
    public void processPaymentPoints(String merchantUid, Integer amount, String impUid) {
        // 1. DB에서 결제 기록 조회
        Payment paymentRecord = paymentRepository.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제 기록입니다."));

        // 2. 이미 처리된 결제인지 확인
        if (paymentRecord.getStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("이미 처리된 결제입니다.");
        }

        // 3. 결제 금액 일치 여부 확인
        if (!paymentRecord.getAmount().equals(amount)) {
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        // 4. 사용자 포인트 업데이트
        User user = paymentRecord.getUser();
        int newTotalPoint = user.getTotalPoint() + amount;
        user.setTotalPoint(newTotalPoint);

        // 5. 포인트 내역(Ledger) 저장
        PointsLedger ledger = PointsLedger.builder()
                .user(user)
                .changeAmount(amount)
                .reason(LedgerReason.PAYMENT)
                .balanceAfter(newTotalPoint)
                .build();

        pointsLedgerRepository.save(ledger);

        // 6. 결제 기록 상태 업데이트
        paymentRecord.setImpUid(impUid);
        paymentRecord.setStatus(PaymentStatus.PAID);
    }
}