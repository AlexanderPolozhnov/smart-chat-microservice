package com.alexander.smartchat.kafka.consumer;

import com.alexander.smartchat.dto.MessageRequestDto;
import com.alexander.smartchat.entity.Chat;
import com.alexander.smartchat.entity.ChatMessage;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.repository.ChatRepository;
import com.alexander.smartchat.repository.MessageRepository;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.service.redis.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumerService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private static final int RECENT_LIMIT = 100;

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "smartchat-group")
    public void consumeMessage(MessageRequestDto dto) {
        executor.submit(() -> processMessage(dto));
    }

    private void processMessage(MessageRequestDto dto) {
        log.info("Обработка сообщения для чата {} от пользователя {}", dto.chatId(), dto.senderId());

        Chat chat = chatRepository.findById(dto.chatId())
            .orElseThrow(() -> new ResourceNotFoundException("Чат не найден: " + dto.chatId()));
        User sender = userRepository.findById(dto.senderId())
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + dto.senderId()));

        ChatMessage message = ChatMessage.builder()
            .chat(chat)
            .sender(sender)
            .text(dto.text())
            .sentAt(Instant.now())
            .build();

        ChatMessage saved = messageRepository.save(message);

        redisCacheService.cacheMessage(chat.getId(), saved, RECENT_LIMIT);

        log.info("Сообщение сохранено и добавлено в кеш чата {}", chat.getId());
    }
}

