package com.alexander.smartchat.service.kafka.producer;

import com.alexander.smartchat.dto.MessageRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
@Setter
public class MessageProducerService {

    private final KafkaTemplate<String, MessageRequestDto> kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topic;

    public void sendMessage(MessageRequestDto message) {
        kafkaTemplate.send(topic, message.chatId().toString(), message);
        log.info("Сообщение отправлено в Kafka: {}", message);
    }
}
