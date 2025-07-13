package com.alexander.smartchat.dto;

import com.alexander.smartchat.entity.Role;

import java.util.UUID;

public record UserResponseDto(
    UUID id,
    Role role,
    String username,
    String email
) {
}
