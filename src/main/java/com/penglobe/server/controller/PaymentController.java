// src/main/java/com/penglobe/server/controller/PaymentController.java
package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.service.PaymentService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "아임포트를 이용한 결제 API")
public class PaymentController {

    private final PaymentService paymentService;
    private IamportClient iamportClient;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    // 결제 전, 결제 기록을 DB에 저장하는 API (프론트엔드에서 먼저 호출)
    @PostMapping("/prepare")
    @Operation(summary = "결제 준비", description = "결제 전 서버에서 고유 주문번호(merchant_uid)를 생성합니다.")
    public ResponseEntity<ApiResponse<String>> preparePayment(
            @RequestBody Map<String, Integer> payload,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        Integer amount = payload.get("amount");

        if (amount == null || amount <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "유효한 금액이 필요합니다."));
        }

        try {
            String merchantUid = paymentService.preparePayment(userId, amount);
            return ResponseEntity.ok(ApiResponse.success(200, "결제 준비 성공", merchantUid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail(500, "결제 준비 중 오류 발생: " + e.getMessage()));
        }
    }

    // 최종 결제 검증 및 포인트 충전 API (프론트엔드에서 결제 완료 후 호출)
    @PostMapping("/verify/{impUid}")
    @Operation(
            summary = "결제 검증",
            description = "결제 고유번호(impUid)와 주문번호(merchantUid)로 결제 정보를 확인하고 검증합니다."
    )
    public ResponseEntity<ApiResponse<Void>> verifyPayment(
            @Parameter(description = "클라이언트가 전달한 결제 고유번호", required = true, example = "imp_123456789")
            @PathVariable String impUid,
            @RequestBody Map<String, String> payload, // merchantUid를 바디로 받음
            Authentication authentication
    ) {
        String merchantUid = payload.get("merchant_uid");
        if (merchantUid == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "주문번호(merchant_uid)가 필요합니다."));
        }

        try {
            // 1. 아임포트에서 실제 결제 정보 조회
            IamportResponse<Payment> paymentResponse = iamportClient.paymentByImpUid(impUid);
            Payment payment = paymentResponse.getResponse();

            if (payment == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(404, "아임포트에 결제 정보가 존재하지 않습니다."));
            }

            // 결제 성공 상태인지 확인
            if (!"paid".equalsIgnoreCase(payment.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail(400, "결제가 완료되지 않았습니다. status=" + payment.getStatus()));
            }

            // 주문번호 일치 확인 (위조/오류 방지)
            if (payment.getMerchantUid() == null || !payment.getMerchantUid().equals(merchantUid)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.fail(400, "merchant_uid가 일치하지 않습니다."));
            }

            // 2. DB에 저장된 금액과 아임포트 결제 금액 교차 검증
            // processPaymentPoints 메서드에서 금액 일치 여부를 검증
            paymentService.processPaymentPoints(merchantUid, payment.getAmount().intValue(), impUid);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(200, "결제 검증 및 포인트 충전 성공", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, "결제 검증 실패: " + e.getMessage()));
        }
    }
}