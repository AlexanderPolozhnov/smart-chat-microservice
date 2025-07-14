package com.alexander.smartchat.kafka.consumer;

import com.alexander.smartchat.dto.MessageRequestDto;
import com.alexander.smartchat.entity.Chat;
import com.alexander.smartchat.entity.ChatMessage;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.repository.ChatMessageRepository;
import com.alexander.smartchat.repository.ChatRepository;
import com.alexander.smartchat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumerService {

    private final ChatMessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, ChatMessage> redisTemplate;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

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

        String key = "chat:" + dto.chatId() + ":recent";
        redisTemplate.opsForList().leftPush(key, saved);
        redisTemplate.opsForList().trim(key, 0, 99);

        log.info("Сообщение сохраняется и кэшируется под ключом {}", key);
    }
}
