package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.JwtResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisTemplate<String, JwtResponse> tokenRedisTemplate;

    private final RedisTemplate<String, JwtResponse> blacklistRedisTemplate;

    public void saveTokenBlackList(String username, JwtResponse jwtResponse) {
        blacklistRedisTemplate.opsForValue().set(username, jwtResponse);
    }

    public Optional<JwtResponse> getTokenBlackList(String username) {
        return Optional.ofNullable(blacklistRedisTemplate.opsForValue().get(username));
    }

    public void saveUserToken(String userId, JwtResponse jwtResponse) {
        tokenRedisTemplate.opsForHash().put("user_tokens", userId, jwtResponse);
    }

    public JwtResponse getUserToken(String userId) {
        return (JwtResponse) tokenRedisTemplate.opsForHash().get("user_tokens", userId);
    }
}
