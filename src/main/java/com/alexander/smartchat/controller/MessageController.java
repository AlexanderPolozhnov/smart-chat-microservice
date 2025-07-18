package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.dto.MessageSearchDto;
import com.alexander.smartchat.dto.MessageStatsDto;
import com.alexander.smartchat.entity.ChatMessage;
import com.alexander.smartchat.service.MessageService;
import com.alexander.smartchat.service.redis.RedisCacheService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Чтение сообщений", description = "Получение истории, поиск и статистика по сообщениям")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final RedisCacheService redisCacheService;

    @GetMapping
    public ResponseEntity<List<MessageResponseDto>> getMessages(
        @RequestParam UUID chatId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(messageService.getMessages(chatId, limit));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ChatMessage>> getRecent(
        @RequestParam UUID chatId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(redisCacheService.getRecentMessages(chatId, limit));
    }


    @GetMapping("/search")
    public ResponseEntity<MessageSearchDto> search(
        @RequestParam UUID chatId,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(messageService.search(chatId, keyword, limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<MessageStatsDto> getStats(@RequestParam UUID chatId) {
        return ResponseEntity.ok(messageService.getStats(chatId));
    }
}
