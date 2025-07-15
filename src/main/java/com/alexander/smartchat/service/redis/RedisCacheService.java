package com.alexander.smartchat.service.redis;

import com.alexander.smartchat.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Setter
public class RedisCacheService {

    private final RedisTemplate<String, ChatMessage> messageRedisTemplate;

    @Value("${chat.message-cache-ttl}")
    private Duration messageCacheTtl;

    private static String recentKey(UUID chatId) {
        return "chat:" + chatId + ":recent";
    }

    public void cacheMessage(UUID chatId, ChatMessage message, int limit) {
        String key = recentKey(chatId);

        messageRedisTemplate.opsForList().leftPush(key, message);
        messageRedisTemplate.opsForList().trim(key, 0, limit - 1);

        if (!messageRedisTemplate.hasKey(key)) {
            messageRedisTemplate.expire(key, messageCacheTtl);
        }
    }

    public List<ChatMessage> getRecentMessages(UUID chatId, int limit) {
        String key = recentKey(chatId);
        return messageRedisTemplate.opsForList().range(key, 0, limit - 1);
    }

    public void clearChatCache(UUID chatId) {
        messageRedisTemplate.delete(recentKey(chatId));
    }
}


