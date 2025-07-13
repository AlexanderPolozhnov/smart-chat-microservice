package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.*;
import com.alexander.smartchat.service.AuthService;
import com.alexander.smartchat.service.BlacklistTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final BlacklistTokenService blacklistTokenService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto created = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<LoginResponse> getNewAccessToken(@RequestBody RefreshJwtToken refreshJwtToken) {
        return ResponseEntity.ok(authService.getAccessToken(refreshJwtToken.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String header) {
        blacklistTokenService.saveTokenBlackList(header);
        return ResponseEntity.ok().build();
    }
}
