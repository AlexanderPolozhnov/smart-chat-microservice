package com.alexander.smartchat.service;

import com.alexander.smartchat.entity.ChatMessage;
import com.alexander.smartchat.service.redis.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, ChatMessage> redisTemplate;

    @Mock
    private ListOperations<String, ChatMessage> listOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    private UUID chatId;
    private ChatMessage message;
    private Duration ttl = Duration.ofMinutes(10);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatId = UUID.randomUUID();
        message = new ChatMessage();
        redisCacheService.setMessageCacheTtl(ttl);

        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void cacheMessage_ShouldPushAndTrimList_AndSetExpireIfKeyAbsent() {
        String expectedKey = "chat:" + chatId + ":recent";
        int limit = 5;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(false);

        redisCacheService.cacheMessage(chatId, message, limit);

        InOrder inOrder = inOrder(listOperations, redisTemplate);

        inOrder.verify(listOperations).leftPush(expectedKey, message);
        inOrder.verify(listOperations).trim(expectedKey, 0, limit - 1);
        inOrder.verify(redisTemplate).hasKey(expectedKey);
        inOrder.verify(redisTemplate).expire(expectedKey, ttl);
    }

    @Test
    void cacheMessage_ShouldNotSetExpireIfKeyExists() {
        String expectedKey = "chat:" + chatId + ":recent";
        int limit = 5;

        when(redisTemplate.hasKey(expectedKey)).thenReturn(true);

        redisCacheService.cacheMessage(chatId, message, limit);

        verify(listOperations).leftPush(expectedKey, message);
        verify(listOperations).trim(expectedKey, 0, limit - 1);
        verify(redisTemplate).hasKey(expectedKey);
        verify(redisTemplate, never()).expire(anyString(), any());
    }

    @Test
    void getRecentMessages_ShouldReturnRangeFromRedis() {
        String expectedKey = "chat:" + chatId + ":recent";
        int limit = 10;

        List<ChatMessage> expectedMessages = List.of(message);
        when(listOperations.range(expectedKey, 0, limit - 1)).thenReturn(expectedMessages);

        List<ChatMessage> actualMessages = redisCacheService.getRecentMessages(chatId, limit);

        assertEquals(expectedMessages, actualMessages);
        verify(listOperations).range(expectedKey, 0, limit - 1);
    }

    @Test
    void clearChatCache_ShouldDeleteKey() {
        String expectedKey = "chat:" + chatId + ":recent";

        redisCacheService.clearChatCache(chatId);

        verify(redisTemplate).delete(expectedKey);
    }
}
