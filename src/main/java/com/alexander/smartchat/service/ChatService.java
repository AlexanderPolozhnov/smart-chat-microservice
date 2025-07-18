package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.ChatRequestDto;
import com.alexander.smartchat.dto.ChatResponseDto;
import com.alexander.smartchat.entity.Chat;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.mapper.ChatMapper;
import com.alexander.smartchat.repository.ChatRepository;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.service.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final RedisCacheService redisCacheService;

    public List<ChatResponseDto> getUserChats(UUID userId) {
        return chatRepository.findAll().stream()
            .filter(chat -> chat.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId)))
            .map(chatMapper::toDto)
            .toList();
    }

    @Transactional
    public ChatResponseDto createChat(ChatRequestDto requestDto) {
        Chat chat = chatMapper.toEntity(requestDto);
        Set<User> users = new HashSet<>(userRepository.findAllById(requestDto.userIds()));
        chat.setUsers(users);
        return chatMapper.toDto(chatRepository.save(chat));
    }

    @Transactional
    public void addUserToChat(UUID chatId, UUID userId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Чат с id " + chatId + " не был найден"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + userId + " не был найден"));
        chat.getUsers().add(user);
    }

    @Transactional
    public void deleteChat(UUID chatId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ResourceNotFoundException("Чат с id " + chatId + " не был найден"));
        chatRepository.delete(chat);
        redisCacheService.clearChatCache(chatId);
    }
}
