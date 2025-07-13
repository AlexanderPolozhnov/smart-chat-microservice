package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.*;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.exception.AuthException;
import com.alexander.smartchat.mapper.UserMapper;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.service.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    public UserResponseDto register(@NonNull UserRequestDto request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AuthException("Имя пользователя уже занято");
        }

        User user = User.builder()
            .username(request.username())
            .password(passwordEncoder.encode(request.password()))
            .email(request.email())
            .role(Role.USER)
            .build();
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public LoginResponse login(@NonNull LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.username())
            .orElseThrow(() -> new AuthException("Пользователь не был найден"));

        if (passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            final String accessToken = jwtProvider.generateAccessToken(user);
            final String refreshToken = jwtProvider.generateRefreshToken(user);

            tokenService.saveUserToken(user.getUsername(), new JwtResponse(accessToken, refreshToken));
            return new LoginResponse(accessToken, refreshToken);
        } else {
            throw new AuthException("Пароль не корректный");
        }
    }

    public LoginResponse getAccessToken(@NonNull String refreshToken) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new AuthException("Токен обновления недействителен или истек");
        }
        if (jwtProvider.validateRefreshToken(refreshToken)) {
            Claims claims = jwtProvider.getRefreshClaims(refreshToken);
            String username = claims.getSubject();
            JwtResponse userToken = tokenService.getUserToken(username);

            if (userToken != null && userToken.refreshToken().equals(refreshToken)) {
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthException("Пользователь не был найден"));

                final String accessToken = jwtProvider.generateAccessToken(user);
                return new LoginResponse(accessToken, null);
            }
        }
        return new LoginResponse(null, null);
    }
}
