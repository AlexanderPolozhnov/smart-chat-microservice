package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.MessageRequestDto;
import com.alexander.smartchat.entity.Chat;
import com.alexander.smartchat.entity.ChatMessage;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.repository.ChatRepository;
import com.alexander.smartchat.repository.MessageRepository;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.service.kafka.consumer.MessageConsumerService;
import com.alexander.smartchat.service.redis.RedisCacheService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageConsumerServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private MessageConsumerService consumerService;

    private final UUID chatId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void consumeMessage_shouldProcessAndCacheMessage() {
        MessageRequestDto dto = new MessageRequestDto(chatId, userId, "Test message");
        Chat chat = Chat.builder().id(chatId).build();
        User sender = User.builder().id(userId).build();
        ChatMessage savedMessage = ChatMessage.builder().chat(chat).sender(sender).text("Test message").build();

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(userId)).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

        consumerService.consumeMessage(dto);

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        verify(messageRepository).save(any(ChatMessage.class));
        verify(redisCacheService).cacheMessage(eq(chatId), any(ChatMessage.class), eq(100));
    }
}
