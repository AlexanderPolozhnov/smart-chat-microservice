package com.alexander.smartchat.dto;

import java.util.UUID;

public record MessageRequestDto(
    UUID chatId,
    UUID senderId,
    String text
) {
}
