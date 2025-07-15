package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.ChatRequestDto;
import com.alexander.smartchat.dto.ChatResponseDto;
import com.alexander.smartchat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<List<ChatResponseDto>> getUserChats(@RequestParam UUID userId) {
        return ResponseEntity.ok(chatService.getUserChats(userId));
    }

    @PostMapping
    public ResponseEntity<ChatResponseDto> createChat(@Valid @RequestBody ChatRequestDto requestDto) {
        ChatResponseDto responseDto = chatService.createChat(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/{chatId}/users/{userId}")
    public ResponseEntity<Void> addUserToChat(@PathVariable UUID chatId, @PathVariable UUID userId) {
        chatService.addUserToChat(chatId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{chatId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID chatId) {
        chatService.deleteChat(chatId);
        return ResponseEntity.noContent().build();
    }
}
