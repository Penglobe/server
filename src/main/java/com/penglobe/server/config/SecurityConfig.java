package com.penglobe.server.config;

import com.penglobe.server.security.JwtAuthenticationFilter;
import com.penglobe.server.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // main
                        .requestMatchers("/", "/index.html").permitAll()
                        // static resources 허용
                        .requestMatchers("/images/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        // Swagger / API Docs
                        .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                        // Auth
                        .requestMatchers("/auth/**").permitAll()
                        // Kakao map & proxy
                        .requestMatchers(HttpMethod.GET, "/map").permitAll()
                        .requestMatchers("/api/kakao/**").permitAll()
                        // Shop, Ranking, Uploads
                        .requestMatchers(HttpMethod.GET,
                                "/shop/products", "/shop/products/**",
                                "/rankings/regions", "/uploads/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        // OPTIONS (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 퀴즈
                        .requestMatchers("/quiz/**").permitAll()
                        // 나머지는 인증 필수
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED)) // 401
                        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))          // 403
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class)
                .httpBasic(h -> h.disable())
                .formLogin(f -> f.disable());

        return http.build();
    }
}
