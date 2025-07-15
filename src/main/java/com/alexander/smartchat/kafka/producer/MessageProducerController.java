package com.alexander.smartchat.kafka.producer;

import com.alexander.smartchat.dto.MessageRequestDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Producer сообщений", description = "Отправка новых сообщений в Kafka")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageProducerController {

    private final MessageProducerService messageProducerService;

    @PostMapping
    public ResponseEntity<Void> sendMessage(@Valid @RequestBody MessageRequestDto messageRequestDto) {
        messageProducerService.sendMessage(messageRequestDto);
        return ResponseEntity.accepted().build();
    }
}
