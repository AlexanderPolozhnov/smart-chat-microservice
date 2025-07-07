package com.alexander.smartchat.dto;

public record JwtResponse(
    String accessToken,
    String refreshToken,
    String tokenType
) {
}
