package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.JwtResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TokenServiceTest {

    @Mock
    private RedisTemplate<String, JwtResponse> tokenRedisTemplate;

    @Mock
    private ValueOperations<String, JwtResponse> valueOperations;

    @Mock
    private HashOperations<String, Object, JwtResponse> hashOperations;

    @InjectMocks
    private TokenService tokenService;

    private final String USERNAME = "alice";
    private final String USER_ID = "user-123";
    private final JwtResponse JWT = new JwtResponse("access", "refresh");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(tokenRedisTemplate.opsForValue()).thenReturn(valueOperations);
        doReturn(hashOperations).when(tokenRedisTemplate).opsForHash();
    }

    @Test
    @DisplayName("saveTokenBlackList должен сохранять токен в черном списке Redis")
    void saveTokenBlackList_ShouldCallValueOperationsSet() {
        tokenService.saveTokenBlackList(USERNAME, JWT);

        verify(tokenRedisTemplate).opsForValue();
        verify(valueOperations).set(USERNAME, JWT);
        verifyNoMoreInteractions(valueOperations, tokenRedisTemplate);
    }

    @Test
    @DisplayName("getTokenBlackList должен возвращать Optional из JwtResponse, если он присутствует")
    void getTokenBlackList_WhenPresent_ShouldReturnOptional() {
        when(valueOperations.get(USERNAME)).thenReturn(JWT);
        Optional<JwtResponse> result = tokenService.getTokenBlackList(USERNAME);

        assertThat(result).contains(JWT);

        verify(tokenRedisTemplate).opsForValue();
        verify(valueOperations).get(USERNAME);
    }

    @Test
    @DisplayName("getTokenBlackList должен возвращать пустой Optional, если он отсутствует")
    void getTokenBlackList_WhenNotPresent_ShouldReturnEmpty() {
        when(valueOperations.get(USERNAME)).thenReturn(null);
        Optional<JwtResponse> result = tokenService.getTokenBlackList(USERNAME);

        assertThat(result).isEmpty();

        verify(tokenRedisTemplate).opsForValue();
        verify(valueOperations).get(USERNAME);
    }

    @Test
    @DisplayName("saveUserToken должен помещать JwtResponse в хэш user_tokens")
    void saveUserToken_ShouldCallHashOperationsPut() {
        tokenService.saveUserToken(USER_ID, JWT);

        verify(tokenRedisTemplate).opsForHash();
        verify(hashOperations).put("user_tokens", USER_ID, JWT);
        verifyNoMoreInteractions(hashOperations, tokenRedisTemplate);
    }

    @Test
    @DisplayName("getUserToken должен возвращать JwtResponse из хэша user_tokens")
    void getUserToken_ShouldReturnJwtResponse() {
        when(hashOperations.get("user_tokens", USER_ID)).thenReturn(JWT);
        JwtResponse result = tokenService.getUserToken(USER_ID);

        assertThat(result).isEqualTo(JWT);

        verify(tokenRedisTemplate).opsForHash();
        verify(hashOperations).get("user_tokens", USER_ID);
    }
}

