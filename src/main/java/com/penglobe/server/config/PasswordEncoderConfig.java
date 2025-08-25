package com.penglobe.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 가장 흔히 사용하는 Bcrypt
        return new BCryptPasswordEncoder();
        // 또는 위 대신 DelegatingPasswordEncoder를 쓰고 싶다면:
        // return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
