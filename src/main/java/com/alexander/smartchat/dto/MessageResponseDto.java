package com.alexander.smartchat.dto;

import java.time.Instant;
import java.util.UUID;

public record MessageResponseDto(
    UUID id,
    UUID chatId,
    UUID senderId,
    String text,
    Instant sentAt
) {
}
