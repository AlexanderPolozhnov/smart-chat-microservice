package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.*;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.exception.AuthException;
import com.alexander.smartchat.mapper.UserMapper;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.service.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthService authService;

    private UserRequestDto buildRegisterRequest() {
        return new UserRequestDto("test", "pass123", "test@example.com");
    }

    private User buildUserEntity() {
        return User.builder()
            .username("test")
            .password("encodedPass")
            .email("test@example.com")
            .role(Role.USER)
            .build();
    }

    @Test
    @DisplayName("Регистрация должна возвращать DTO пользователя, если имя пользователя не занято")
    void register_ShouldReturnUserResponse_WhenUsernameNotTaken() {
        UserRequestDto req = buildRegisterRequest();
        User entity = buildUserEntity();
        UserResponseDto dto = new UserResponseDto(entity.getId(), entity.getRole(), entity.getUsername(),
            entity.getEmail());

        when(userRepository.existsByUsername("test")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(entity);
        when(userMapper.toDto(entity)).thenReturn(dto);

        UserResponseDto result = authService.register(req);

        assertEquals(dto, result);
        verify(userRepository).existsByUsername("test");
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDto(entity);
    }

    @Test
    @DisplayName("Регистрация должна выбрасывать исключение, если имя пользователя занято")
    void register_ShouldThrowAuthException_WhenUsernameTaken() {
        UserRequestDto req = buildRegisterRequest();
        when(userRepository.existsByUsername("test")).thenReturn(true);

        AuthException ex = assertThrows(AuthException.class, () -> authService.register(req));
        assertEquals("Имя пользователя уже занято", ex.getMessage());
        verify(userRepository).existsByUsername("test");
        verifyNoMoreInteractions(userRepository, passwordEncoder, userMapper);
    }

    @Test
    @DisplayName("Авторизация должна возвращать токены, если переданы валидные учётные данные")
    void login_ShouldReturnLoginResponse_WhenValidCredentials() {
        LoginRequest loginReq = new LoginRequest("bob", "secret");
        User user = User.builder().username("bob").password("hashed").role(Role.USER).build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtProvider.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken(user)).thenReturn("refreshToken");

        LoginResponse resp = authService.login(loginReq);

        assertEquals("accessToken", resp.getAccessToken());
        assertEquals("refreshToken", resp.getRefreshToken());
        verify(tokenService).saveUserToken("bob", new JwtResponse("accessToken", "refreshToken"));
    }

    @Test
    @DisplayName("Авторизация должна выбрасывать исключение, если пользователь не найден")
    void login_ShouldThrowAuthException_WhenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        AuthException ex = assertThrows(AuthException.class,
            () -> authService.login(new LoginRequest("unknown", "any")));
        assertEquals("Пользователь не был найден", ex.getMessage());
    }

    @Test
    @DisplayName("Авторизация должна выбрасывать исключение, если пароль неверный")
    void login_ShouldThrowAuthException_WhenPasswordInvalid() {
        User user = User.builder().username("bob").password("hashed").build();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class, () -> authService.login(new LoginRequest("bob", "wrong")));
        assertEquals("Пароль не корректный", ex.getMessage());
    }

    @Test
    @DisplayName("Получение access токена должно выбрасывать исключение, если refresh токен недействителен")
    void getAccessToken_ShouldThrowAuthException_WhenRefreshInvalid() {
        when(jwtProvider.validateRefreshToken("badToken")).thenReturn(false);

        AuthException ex = assertThrows(AuthException.class, () -> authService.getAccessToken("badToken"));
        assertEquals("Токен обновления недействителен или истек", ex.getMessage());
    }

    @Test
    @DisplayName("Получение access токена должно вернуть новый токен, если refresh валиден и совпадает")
    void getAccessToken_ShouldReturnNewAccessToken_WhenValidAndMatches() {
        String refresh = "validRefresh";
        Claims claims = mock(Claims.class);
        when(jwtProvider.validateRefreshToken(refresh)).thenReturn(true);
        when(jwtProvider.getRefreshClaims(refresh)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("carol");
        when(tokenService.getUserToken("carol")).thenReturn(new JwtResponse("oldAccess", "validRefresh"));

        User user = User.builder().username("carol").build();
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(user)).thenReturn("newAccess");

        LoginResponse resp = authService.getAccessToken(refresh);

        assertEquals("newAccess", resp.getAccessToken());
        assertNull(resp.getRefreshToken());
    }

    @Test
    @DisplayName("Получение access токена должно вернуть null, если refresh валиден, но не совпадает")
    void getAccessToken_ShouldReturnNullTokens_WhenValidButMismatch() {
        String refresh = "validRefresh";
        Claims claims = mock(Claims.class);
        when(jwtProvider.validateRefreshToken(refresh)).thenReturn(true);
        when(jwtProvider.getRefreshClaims(refresh)).thenReturn(claims);
        when(claims.getSubject()).thenReturn("dave");
        when(tokenService.getUserToken("dave")).thenReturn(new JwtResponse("oldAccess", "otherRefresh"));

        LoginResponse resp = authService.getAccessToken(refresh);

        assertNull(resp.getAccessToken());
        assertNull(resp.getRefreshToken());
    }
}
