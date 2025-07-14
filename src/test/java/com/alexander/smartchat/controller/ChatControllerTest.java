package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.ChatRequestDto;
import com.alexander.smartchat.dto.ChatResponseDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.exception.GlobalExceptionHandler;
import com.alexander.smartchat.exception.ResourceNotFoundException;
import com.alexander.smartchat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID userId = UUID.randomUUID();
    private final UUID chatId = UUID.randomUUID();

    private ChatRequestDto buildRequestDto() {
        return new ChatRequestDto("JavaTeamChat", Set.of(userId));
    }

    private ChatResponseDto buildResponseDto() {
        UserResponseDto userDto = new UserResponseDto(userId, Role.ADMIN, "testuser", "test@example.com");
        return new ChatResponseDto(chatId, "JavaTeamChat", Set.of(userDto));
    }


    @Test
    @DisplayName("GET /api/chats?userId -> 200 OK with chat list")
    @WithMockUser(username = "test", roles = "USER")
    void getUserChats_ShouldReturnChats() throws Exception {
        when(chatService.getUserChats(userId)).thenReturn(List.of(buildResponseDto()));

        mockMvc.perform(get("/api/chats")
                .param("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(chatId.toString()));

        verify(chatService).getUserChats(userId);
    }

    @Test
    @DisplayName("POST /api/chats -> 201 Created with chat response")
    @WithMockUser(username = "test", roles = "USER")
    void createChat_ShouldReturnCreated() throws Exception {
        ChatRequestDto requestDto = buildRequestDto();
        ChatResponseDto responseDto = buildResponseDto();

        when(chatService.createChat(any(ChatRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/chats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(chatId.toString()))
            .andExpect(jsonPath("$.name").value("JavaTeamChat"));

        verify(chatService).createChat(any(ChatRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/chats/{chatId}/users/{userId} -> 200 OK")
    @WithMockUser(username = "test", roles = "USER")
    void addUserToChat_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/chats/{chatId}/users/{userId}", chatId, userId))
            .andExpect(status().isOk());

        verify(chatService).addUserToChat(chatId, userId);
    }

    @Test
    @DisplayName("DELETE /api/chats/{chatId} -> 204 No Content when admin")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteChat_ShouldReturnNoContent_WhenAdmin() throws Exception {
        mockMvc.perform(delete("/api/chats/{chatId}", chatId))
            .andExpect(status().isNoContent());

        verify(chatService).deleteChat(chatId);
    }

    @Test
    @DisplayName("DELETE /api/chats/{chatId} -> 403 Forbidden when not admin")
    @WithMockUser(username = "user", roles = "USER")
    void deleteChat_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        mockMvc.perform(delete("/api/chats/{chatId}", chatId))
            .andExpect(status().isForbidden());

        verify(chatService, never()).deleteChat(any());
    }

    @Test
    @DisplayName("DELETE /api/chats/{chatId} -> 404 when chat not found")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteChat_ShouldReturnNotFound_WhenChatMissing() throws Exception {
        doThrow(new ResourceNotFoundException("„ат с id " + chatId + " не найден"))
            .when(chatService).deleteChat(chatId);

        mockMvc.perform(delete("/api/chats/{chatId}", chatId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("„ат с id " + chatId + " не найден"));

        verify(chatService).deleteChat(chatId);
    }
}
