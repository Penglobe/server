package com.penglobe.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PortOneClient {

    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    // ✅ 프로퍼티 키를 네 파일에 맞춤 + api-url 기본값 제공
    @Value("${portone.api-url:https://api.iamport.kr}")
    private String apiBase;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.api-secret}")
    private String apiSecret;

    private String cachedToken;
    private long tokenExpEpoch;

    public synchronized String getAccessToken() {
        long now = Instant.now().getEpochSecond();
        if (cachedToken != null && now < tokenExpEpoch - 30) return cachedToken;

        String url = apiBase + "/users/getToken"; // v1 토큰
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("imp_key", apiKey, "imp_secret", apiSecret), headers);

        ResponseEntity<Map> resp = rest.postForEntity(url, entity, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null)
            throw new IllegalStateException("PortOne token fail: " + resp.getStatusCode());

        Map body = resp.getBody();
        Map data = (Map) body.get("response");
        if (data == null) throw new IllegalStateException("PortOne token empty response");

        this.cachedToken = (String) data.get("access_token");
        Number exp = (Number) data.get("expired_at");
        this.tokenExpEpoch = exp == null ? now + 60 * 30 : exp.longValue();
        return cachedToken;
    }

    private HttpHeaders authHeaders() {
        String token = getAccessToken();
        HttpHeaders headers = new HttpHeaders();
        // ⚠️ Iamport는 예전 문서 기준 "Authorization: {token}" 을 사용.
        // 만약 401이 나면 아래 한 줄로 바꿔서 시험해봐: headers.set("Authorization", "Bearer " + token);
        headers.set("Authorization", token);
        return headers;
    }

    public Map getPaymentByImpUid(String impUid) {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());
        ResponseEntity<Map> resp = rest.exchange(
                apiBase + "/payments/" + impUid, HttpMethod.GET, entity, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null)
            throw new IllegalStateException("PortOne payment lookup fail: " + resp.getStatusCode());
        return resp.getBody();
    }

    public void prepare(String merchantUid, int amount) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(
                Map.of("merchant_uid", merchantUid, "amount", amount), headers);
        ResponseEntity<Map> resp = rest.postForEntity(
                apiBase + "/payments/prepare", entity, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful())
            throw new IllegalStateException("PortOne prepare fail: " + resp.getStatusCode());
    }
}
