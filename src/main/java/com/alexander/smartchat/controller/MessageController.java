package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.dto.MessageSearchDto;
import com.alexander.smartchat.dto.MessageStatsDto;
import com.alexander.smartchat.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<List<MessageResponseDto>> getMessages(
        @RequestParam UUID chatId,
        @RequestParam(defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(messageService.getMessages(chatId, limit));
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
