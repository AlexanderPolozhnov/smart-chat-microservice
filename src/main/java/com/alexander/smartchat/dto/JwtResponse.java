package com.alexander.smartchat.dto;

import java.io.Serializable;

public record JwtResponse(String accessToken, String refreshToken) implements Serializable {
}
