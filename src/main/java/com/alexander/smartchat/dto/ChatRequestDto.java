package com.alexander.smartchat.dto;

import java.util.Set;
import java.util.UUID;

public record ChatRequestDto(String name, Set<UUID> userIds) {
}
