package com.penglobe.server.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoConfig {
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    public String getKakaoApiKey() {
        return kakaoApiKey;
    }
}
