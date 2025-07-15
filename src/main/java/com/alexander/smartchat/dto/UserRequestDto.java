package com.alexander.smartchat.dto;

public record UserRequestDto(
    String username,
    String password,
    String email
) {
}
