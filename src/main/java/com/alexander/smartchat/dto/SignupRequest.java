package com.alexander.smartchat.dto;

public record SignupRequest(
    String username,
    String email,
    String password
) {
}
