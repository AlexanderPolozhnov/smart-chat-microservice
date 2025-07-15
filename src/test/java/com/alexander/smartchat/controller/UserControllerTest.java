package com.alexander.smartchat.controller;

import com.alexander.smartchat.dto.UserRequestDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.exception.GlobalExceptionHandler;
import com.alexander.smartchat.exception.ResourceNotFoundException;
import com.alexander.smartchat.service.UserService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private final UUID EXISTING_ID = UUID.randomUUID();
    private final UUID MISSING_ID = UUID.randomUUID();

    private UserRequestDto buildRequest() {
        return new UserRequestDto("bob", "secretPass", "bob@gmail.com");
    }

    private UserResponseDto buildResponse(UUID id) {
        return new UserResponseDto(id, Role.USER, "bob", "bob@gmail.com");
    }

    @Test
    @DisplayName("GET /api/users -> 200 & list")
    @WithMockUser(username = "test", roles = "USER")
    void getAllUsers_ShouldReturnOkWithList() throws Exception {
        List<UserResponseDto> users = Collections.singletonList(buildResponse(EXISTING_ID));

        when(userService.getAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(EXISTING_ID.toString()))
            .andExpect(jsonPath("$[0].username").value("bob"));

        verify(userService, times(1)).getAll();
    }

    @Test
    @DisplayName("GET /api/users/{id} -> 200 & user when exists")
    @WithMockUser(username = "test", roles = "USER")
    void getUserById_ShouldReturnOk_WhenExists() throws Exception {
        UserResponseDto userResponseDto = buildResponse(EXISTING_ID);

        when(userService.getById(EXISTING_ID)).thenReturn(userResponseDto);

        mockMvc.perform(get("/api/users/{id}", EXISTING_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(EXISTING_ID.toString()))
            .andExpect(jsonPath("$.email").value("bob@gmail.com"));

        verify(userService, times(1)).getById(EXISTING_ID);
    }

    @Test
    @DisplayName("GET /api/users/{id} -> 404 when not exists")
    @WithMockUser(username = "test", roles = "USER")
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(userService.getById(MISSING_ID)).thenThrow(new ResourceNotFoundException(
            "Пользователь с id " + MISSING_ID + " не был найден"));

        mockMvc.perform(get("/api/users/{id}", MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("Пользователь с id " + MISSING_ID + " не был найден"));

        verify(userService, times(1)).getById(MISSING_ID);
    }

    @Test
    @DisplayName("POST /api/users -> 201 & created user")
    @WithMockUser(username = "test", roles = "USER")
    void createUser_ShouldReturnCreated_WhenValid() throws Exception {
        UserRequestDto userRequestDto = buildRequest();
        UserResponseDto userResponseDto = buildResponse(EXISTING_ID);

        when(userService.createUser(userRequestDto)).thenReturn(userResponseDto);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequestDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(EXISTING_ID.toString()))
            .andExpect(jsonPath("$.username").value("bob"));

        verify(userService, times(1)).createUser(any(UserRequestDto.class));
    }

    @Test
    @DisplayName("POST /api/users -> 400 when invalid payload")
    @WithMockUser(username = "test", roles = "USER")
    void createUser_ShouldReturnBadRequest_WhenInvalid() throws Exception {
        var invalid = new UserRequestDto("", "", "email");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
            .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} -> 204 when exists and authorized")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent_WhenExistsAndAdmin() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", EXISTING_ID))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(EXISTING_ID);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} -> 404 when not exists")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteUser_ShouldReturnNotFound_WhenNotExists() throws Exception {
        doThrow(new ResourceNotFoundException("Пользователь с id " + MISSING_ID + " не был найден"))
            .when(userService).deleteUser(MISSING_ID);

        mockMvc.perform(delete("/api/users/{id}", MISSING_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("Пользователь с id " + MISSING_ID + " не был найден"));

        verify(userService).deleteUser(MISSING_ID);
    }

    @Test
    @DisplayName("DELETE /api/users/{id} -> 403 when not admin")
    @WithMockUser(username = "admin", roles = "USER")
    void deleteUser_WhenNotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", EXISTING_ID))
            .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }
}
