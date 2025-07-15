package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.dto.MessageSearchDto;
import com.alexander.smartchat.dto.MessageStatsDto;
import com.alexander.smartchat.exception.GlobalExceptionHandler;
import com.alexander.smartchat.service.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID chatId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/messages -> 200 & list")
    @WithMockUser(username = "test", roles = "USER")
    void getMessages_ShouldReturnList() throws Exception {
        MessageResponseDto dto = new MessageResponseDto(UUID.randomUUID(), chatId, userId, "msg", Instant.now());
        when(messageService.getMessages(eq(chatId), anyInt())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/messages")
                .param("chatId", chatId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].text").value("msg"));

        verify(messageService).getMessages(chatId, 50);
    }

    @Test
    @DisplayName("GET /api/messages/search -> 200 & stats")
    @WithMockUser(username = "test", roles = "USER")
    void search_ShouldReturnSearchDto() throws Exception {
        MessageSearchDto searchDto = new MessageSearchDto(List.of(), 0L);
        when(messageService.search(eq(chatId), eq("txt"), anyInt())).thenReturn(searchDto);

        mockMvc.perform(get("/api/messages/search")
                .param("chatId", chatId.toString())
                .param("keyword", "txt"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(searchDto)));

        verify(messageService).search(chatId, "txt", 50);
    }

    @Test
    @DisplayName("GET /api/messages/stats -> 200 & stats")
    @WithMockUser(username = "test", roles = "USER")
    void getStats_ShouldReturnStatsDto() throws Exception {
        MessageStatsDto statsDto = new MessageStatsDto(5L, Map.of(userId, 3L));
        when(messageService.getStats(eq(chatId))).thenReturn(statsDto);

        mockMvc.perform(get("/api/messages/stats")
                .param("chatId", chatId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalMessages").value(5))
            .andExpect(jsonPath("$.messagesPerUser['" + userId + "']").value(3));

        verify(messageService).getStats(chatId);
    }
}
