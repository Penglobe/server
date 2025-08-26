package com.penglobe.server.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long accessTokenExpMillis;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.access-token-exp}") long accessTokenExpMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpMillis = accessTokenExpMillis;
    }

    public String createToken(Long userId, String role){
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenExpMillis);
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(Map.of("uid", userId, "role", role))
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try { Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public Long getUserId(String token) {
        Object v = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
                .getBody().get("uid");
        return v == null ? null : Long.valueOf(String.valueOf(v));
    }

    public String getRole(String token) {
        Object v = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
                .getBody().get("role");
        return v == null ? null : String.valueOf(v);
    }
}
