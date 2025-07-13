package com.alexander.smartchat.service.jwt;

import com.alexander.smartchat.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    @Value("${jwt.access-validity-minutes}")
    private long accessValidity;
    @Value("${jwt.refresh-validity-days}")
    private long refreshValidity;
    private final Key accessKey;
    private final Key refreshKey;

    public JwtProvider(@Value("${jwt.secret.access}") String aSecret,
                       @Value("${jwt.secret.refresh}") String rSecret) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(aSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(rSecret));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("roles", user.getRole().name())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(accessValidity, ChronoUnit.MINUTES)))
            .signWith(accessKey)
            .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(user.getUsername())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(refreshValidity, ChronoUnit.DAYS)))
            .signWith(refreshKey)
            .compact();
    }

    public boolean validateAccessToken(@NonNull String accessToken) {
        return validateToken(accessToken, accessKey);
    }

    public boolean validateRefreshToken(@NonNull String refreshToken) {
        return validateToken(refreshToken, refreshKey);
    }

    public boolean validateToken(String token, Key key) {
        try {
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, accessKey);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, refreshKey);
    }

    private Claims getClaims(@NonNull String token, @NonNull Key secret) {
        return Jwts.parser()
            .setSigningKey(secret)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
