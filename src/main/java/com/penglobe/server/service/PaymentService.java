package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.dto.payment.ConfirmRequest;
import com.penglobe.server.dto.payment.ConfirmResponse;
import com.penglobe.server.dto.payment.CreateIntentRequest;
import com.penglobe.server.dto.payment.CreateIntentResponse;
import com.penglobe.server.domain.payment.PaymentIntent;
import com.penglobe.server.domain.payment.PaymentStatus;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.PaymentIntentRepository;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.util.PortOneClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentIntentRepository intentRepo;
    private final UserRepository userRepo;
    private final PortOneClient portOne;
    private final PointsLedgerRepository pointsRepo;

    @Value("${app.base-url}")
    private String appBaseUrl;

    // 서버 화이트리스트 (프론트와 동일)
    private static final Set<Integer> ALLOWED_AMOUNTS =
            Set.of(100, 5000, 10000, 20000, 50000);

    @Transactional
    public CreateIntentResponse createIntent(Long userId, CreateIntentRequest req) {
        // --- 유효성 ---
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new IllegalArgumentException("금액 오류");
        }
        if (!ALLOWED_AMOUNTS.contains(req.getAmount())) {
            throw new IllegalArgumentException("허용되지 않은 금액입니다.");
        }
        if (req.getCallback() == null || req.getCallback().isBlank()) {
            throw new IllegalArgumentException("callback은 필수입니다.");
        }

        // --- 사용자 조회 ---
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // --- merchant_uid 서버에서 생성 ---
        String merchantUid = "penglobe_" + user.getUserId() + "_" + System.currentTimeMillis();

        // --- 의도 저장 ---
        PaymentIntent intent = PaymentIntent.builder()
                .user(user)
                .merchantUid(merchantUid)
                .amount(req.getAmount())
                .name(req.getName())
                .callbackUrl(req.getCallback())
                .status(PaymentStatus.PREPARED) // 프로젝트 enum에 맞춤
                .build();
        intentRepo.save(intent);

        // --- 포트원 사전등록(prepare) ---
        portOne.prepare(merchantUid, req.getAmount());

        // --- 결제창 URL ---
        String payUrl = UriComponentsBuilder
                .fromHttpUrl(appBaseUrl.replaceAll("/+$","") + "/payments/checkout")
                .queryParam("mu", merchantUid)
                .queryParam("cb", req.getCallback())
                .build()
                .encode()
                .toUriString();

        return CreateIntentResponse.builder()
                .merchantUid(merchantUid)
                .payUrl(payUrl)
                .build();
    }

    @Transactional
    public ConfirmResponse confirm(Long userId, ConfirmRequest req) {
        if (req.getMerchantUid() == null || req.getMerchantUid().isBlank()) {
            throw new IllegalArgumentException("merchant_uid 누락");
        }

        PaymentIntent intent = intentRepo.lockByMerchantUid(req.getMerchantUid())
                .orElseThrow(() -> new IllegalArgumentException("의도 없음"));

        // 본인 결제만 확정
        if (!intent.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("권한 없음");
        }

        // 이미 완료면 멱등 처리
        if (intent.getStatus() == PaymentStatus.PAID) {
            return ConfirmResponse.builder()
                    .ok(true)
                    .message("이미 완료됨")
                    .newBalance(intent.getUser().getTotalPoint())
                    .build();
        }

        // 실패/취소 콜백
        if (Boolean.FALSE.equals(req.getSuccess())) {
            intent.setStatus(PaymentStatus.FAILED);
            intent.setFailReason("user-cancel-or-fail");
            return ConfirmResponse.builder()
                    .ok(false)
                    .message("결제 실패 또는 취소")
                    .build();
        }

        // 성공 콜백이면 imp_uid 필수
        if (req.getImpUid() == null || req.getImpUid().isBlank()) {
            throw new IllegalArgumentException("imp_uid 누락");
        }

        // --- 포트원 조회로 검증 ---
        Map<?, ?> result = portOne.getPaymentByImpUid(req.getImpUid());
        Map<?, ?> resp = (Map<?, ?>) result.get("response");
        if (resp == null) throw new IllegalStateException("포트원 조회 실패");

        String status = (String) resp.get("status");            // "paid"
        String mu     = (String) resp.get("merchant_uid");
        Number amt    = (Number) resp.get("amount");

        if (!intent.getMerchantUid().equals(mu)) {
            throw new IllegalStateException("주문번호 불일치");
        }
        if (!"paid".equals(status)) {
            throw new IllegalStateException("미결제 상태: " + status);
        }
        if (amt == null || intent.getAmount().intValue() != amt.intValue()) {
            throw new IllegalStateException("금액 불일치");
        }

        // --- 반영 ---
        intent.setImpUid(req.getImpUid());
        intent.setPaidAt(LocalDateTime.now());
        intent.setStatus(PaymentStatus.PAID);

        User u = intent.getUser();
        u.setTotalPoint(u.getTotalPoint() + intent.getAmount());
        userRepo.save(u);

        // --- 포인트 내역 기록 ---
        PointsLedger row = PointsLedger.builder()
                .user(u)
                .changeAmount(intent.getAmount())
                .reason(LedgerReason.TOPUP)
                .build();
        pointsRepo.save(row);

        return ConfirmResponse.builder()
                .ok(true)
                .message("OK")
                .newBalance(u.getTotalPoint())
                .build();
    }

    @Transactional(readOnly = true)
    public PaymentIntent getIntentForCheckout(String merchantUid) {
        return intentRepo.findByMerchantUid(merchantUid)
                .orElseThrow(() -> new IllegalArgumentException("의도 없음"));
    }
}
