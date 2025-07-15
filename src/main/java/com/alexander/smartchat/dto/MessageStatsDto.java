package com.alexander.smartchat.dto;

import java.util.Map;
import java.util.UUID;

public record MessageStatsDto(
    Long totalMessages,
    Map<UUID, Long> messagesPerUser
) {
}
