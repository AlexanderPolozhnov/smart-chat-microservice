package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.MessageRequestDto;
import com.alexander.smartchat.exception.GlobalExceptionHandler;
import com.alexander.smartchat.service.kafka.producer.MessageProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
class MessageProducerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageProducerService messageProducerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test", roles = "USER")
    void sendMessage_ValidRequest_ReturnsAccepted() throws Exception {
        MessageRequestDto dto = new MessageRequestDto(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Test message"
        );

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isAccepted());

        verify(messageProducerService).sendMessage(Mockito.refEq(dto));
    }
}
