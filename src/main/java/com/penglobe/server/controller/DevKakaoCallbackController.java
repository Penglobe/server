package com.penglobe.server.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Hidden
@Profile({"local","dev"})
@RestController
@RequestMapping("/dev")
public class DevKakaoCallbackController {
    private final RestClient rest = RestClient.create();

    @GetMapping("/dev/kakao-callback")
    public String callback(@RequestParam String code) {
        return "auth_code=" + code;
    }

    @PostMapping("/kakao-me")
    public ResponseEntity<?> kakaoMe(@RequestBody Map<String, String> body) {
        String accessToken = body.get("accessToken");
        try {
            Map<?, ?> resp = rest.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(Map.class);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            // ✅ 예외 클래스/메시지/최하위 cause 메시지를 그대로 노출
            String cause = e.getCause() == null ? null : e.getCause().toString();
            return ResponseEntity.status(500).body(Map.of(
                    "errorClass", e.getClass().getName(),
                    "message", String.valueOf(e.getMessage()),
                    "cause", cause
            ));
        }
    }
}
