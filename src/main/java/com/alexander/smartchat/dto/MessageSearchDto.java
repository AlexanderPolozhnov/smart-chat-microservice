package com.alexander.smartchat.dto;

import java.util.List;

public record MessageSearchDto(
    List<MessageResponseDto> messages,
    Long totalMatches
) {
}
