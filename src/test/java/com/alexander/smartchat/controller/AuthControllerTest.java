package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.LoginRequest;
import com.alexander.smartchat.dto.LoginResponse;
import com.alexander.smartchat.dto.UserRequestDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.exception.GlobalExceptionHandler;
import com.alexander.smartchat.service.AuthService;
import com.alexander.smartchat.service.BlacklistTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private BlacklistTokenService blacklistTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signup_ValidRequest_ReturnsCreatedUser() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("alex", "password", "alex@example.com");
        UserResponseDto responseDto = new UserResponseDto(
            java.util.UUID.randomUUID(), Role.USER, "alex", "alex@example.com");

        Mockito.when(authService.register(any(UserRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.username").value("alex"))
            .andExpect(jsonPath("$.email").value("alex@example.com"));

        verify(authService).register(any(UserRequestDto.class));
    }

    @Test
    void login_ValidRequest_ReturnsLoginResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest("alex", "password");
        LoginResponse loginResponse = new LoginResponse("access-token", "refresh-token");

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void getNewAccessToken_ValidRefreshToken_ReturnsNewAccessToken() throws Exception {
        LoginResponse loginResponse = new LoginResponse("new-access-token", "refresh-token");

        Mockito.when(authService.getAccessToken(eq("refresh-token"))).thenReturn(loginResponse);

        mockMvc.perform(post("/auth/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginResponse)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).getAccessToken(eq("refresh-token"));
    }

    @Test
    void logout_ValidToken_ReturnsOk() throws Exception {
        String tokenHeader = "Bearer some.jwt.token";

        mockMvc.perform(post("/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, tokenHeader))
            .andExpect(status().isOk());

        verify(blacklistTokenService).saveTokenBlackList(eq(tokenHeader));
    }
}
