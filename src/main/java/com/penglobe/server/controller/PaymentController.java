package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.payment.ConfirmRequest;
import com.penglobe.server.dto.payment.ConfirmResponse;
import com.penglobe.server.dto.payment.CreateIntentRequest;
import com.penglobe.server.dto.payment.CreateIntentResponse;
import com.penglobe.server.domain.payment.PaymentIntent;
import com.penglobe.server.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "포트원(아임포트) 결제 플로우 API")
public class PaymentController {

    private final PaymentService paymentService;

    // imp 가맹점코드는 프로퍼티에서만 읽는다 (HTML에 직접 주입)
    @Value("${portone.imp-code}")
    private String impMerchantCode;

    @PostMapping("/intents")
    @Operation(
            summary = "결제 의도 생성",
            description = "서버가 merchant_uid를 생성하고 포트원 사전등록(prepare) 수행 후, 결제창 URL을 반환합니다."
    )
    public ResponseEntity<ApiResponse<CreateIntentResponse>> createIntent(
            Authentication authentication,
            @Valid @RequestBody CreateIntentRequest req
    ) {
        Long userId = (authentication == null) ? null : (Long) authentication.getPrincipal();

        CreateIntentResponse data = paymentService.createIntent(userId, req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "결제 의도 생성 성공", data));
    }

    @PostMapping("/confirm")
    @Operation(
            summary = "결제 확정(검증)",
            description = "앱 콜백으로 받은 imp_uid/merchant_uid로 포트원 서버를 조회해 결제 상태·금액·주문번호를 검증하고, 성공 시 포인트를 반영합니다."
    )
    public ResponseEntity<ApiResponse<ConfirmResponse>> confirm(
            Authentication authentication,
            @Valid @RequestBody ConfirmRequest req
    ) {
        Long userId = (authentication == null) ? null : (Long) authentication.getPrincipal();

        ConfirmResponse data = paymentService.confirm(userId, req);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "결제 확정 완료", data));
    }

    @GetMapping(value = "/checkout", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(
            summary = "결제창(서버 렌더링)",
            description = "IMP.request_pay를 호출하는 HTML을 반환합니다. 앱은 WebBrowser.openAuthSessionAsync(payUrl, callback)로 이 페이지를 열고, 완료 후 callback 딥링크로 돌아갑니다."
    )
    public ResponseEntity<String> checkout(
            @Parameter(description = "서버가 생성한 merchant_uid", required = true, example = "penglobe_1_1736651234567")
            @RequestParam("mu") String merchantUid,
            @Parameter(description = "앱 딥링크 콜백 URL (예: penglobe://pay/callback)", required = true, example = "penglobe://pay/callback")
            @RequestParam("cb") String callback
    ) {
        PaymentIntent intent = paymentService.getIntentForCheckout(merchantUid);

        // PG는 고정값(요청대로 html5_inicis)
        String html = """
                <!doctype html>
                <html lang="ko">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <script src="https://cdn.iamport.kr/v1/iamport.js"></script>
                  <title>Penglobe 결제</title>
                </head>
                <body>
                <script>
                  const IMP_MERCHANT = %s;            // 예: "imp22234788" (프로퍼티에서 읽음)
                  const PG = "html5_inicis";          // 고정
                  const MERCHANT_UID = %s;
                  const NAME = %s;
                  const AMOUNT = %d;
                  const CALLBACK = %s;

                  function escUrl(u){ try { return new URL(u).toString(); } catch(e){ return CALLBACK; } }

                  IMP.init(IMP_MERCHANT);
                  IMP.request_pay({
                    pg: PG,
                    pay_method: "card",
                    merchant_uid: MERCHANT_UID,
                    name: NAME,
                    amount: AMOUNT
                  }, function(rsp) {
                    try {
                      const u = new URL(escUrl(CALLBACK));
                      u.searchParams.set("success", rsp.success ? "true" : "false");
                      u.searchParams.set("merchant_uid", rsp.merchant_uid || MERCHANT_UID);
                      if (rsp.success) {
                        u.searchParams.set("imp_uid", rsp.imp_uid);
                      } else if (rsp.error_msg) {
                        u.searchParams.set("error_msg", rsp.error_msg);
                      }
                      // 앱으로 복귀
                      location.replace(u.toString());
                    } catch(e) {
                      alert("콜백 이동 실패: " + e.message);
                    }
                  });
                </script>
                </body>
                </html>
                """.formatted(
                js(impMerchantCode),
                js(intent.getMerchantUid()),
                js(intent.getName() == null ? "얼음 충전" : intent.getName()),
                intent.getAmount(),
                js(callback)
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // 간단한 JS 문자열 이스케이프
    private static String js(String s) {
        if (s == null) return "null";
        String esc = s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "");
        return "\"" + esc + "\"";
    }
}
