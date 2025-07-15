package com.alexander.smartchat.dto;

import java.util.Set;
import java.util.UUID;

public record ChatResponseDto(UUID id, String name, Set<UserResponseDto> users) {
}
