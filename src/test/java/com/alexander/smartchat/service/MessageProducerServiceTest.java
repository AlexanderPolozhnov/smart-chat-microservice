package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.MessageRequestDto;
import com.alexander.smartchat.service.kafka.producer.MessageProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageProducerServiceTest {

    @Mock
    private KafkaTemplate<String, MessageRequestDto> kafkaTemplate;

    @InjectMocks
    private MessageProducerService producerService;

    @BeforeEach
    void setUp() {
        producerService.setTopic("test-topic");
    }

    @Test
    void sendMessage_shouldSendToKafka() {
        MessageRequestDto dto = new MessageRequestDto(UUID.randomUUID(), UUID.randomUUID(), "hello");

        producerService.sendMessage(dto);

        verify(kafkaTemplate, times(1)).send("test-topic", dto.chatId().toString(), dto);
    }
}
