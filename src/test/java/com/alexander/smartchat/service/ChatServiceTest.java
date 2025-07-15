package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.ChatRequestDto;
import com.alexander.smartchat.dto.ChatResponseDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.Chat;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.mapper.ChatMapper;
import com.alexander.smartchat.repository.ChatRepository;
import com.alexander.smartchat.repository.UserRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatService chatService;

    private UUID userId;
    private UUID chatId;
    private User user;
    private Chat chat;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        chatId = UUID.randomUUID();
        user = User.builder()
            .id(userId)
            .username("user1")
            .password("test_password")
            .email("test@gmail.com")
            .build();
        chat = Chat.builder()
            .id(chatId)
            .name("test chat")
            .users(new HashSet<>(Set.of(user)))
            .build();
    }

    @Test
    @DisplayName("getUserChats должен вернуть список чатов, в которых есть пользователь")
    void getUserChats_ShouldReturnUserChats() {
        when(chatRepository.findAll()).thenReturn(List.of(chat));
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getRole(), user.getUsername(),
            user.getEmail());
        ChatResponseDto responseDto = new ChatResponseDto(chat.getId(), chat.getName(), Set.of(userResponseDto));
        when(chatMapper.toDto(chat)).thenReturn(responseDto);

        List<ChatResponseDto> result = chatService.getUserChats(userId);

        assertEquals(1, result.size());
        assertEquals(chat.getId(), result.get(0).id());
        verify(chatRepository).findAll();
        verify(chatMapper).toDto(chat);
    }

    @Test
    @DisplayName("createChat должен создать и вернуть новый чат с пользователями")
    void createChat_ShouldCreateChat() {
        ChatRequestDto requestDto = new ChatRequestDto("New Chat", Set.of(userId));
        Chat newChat = Chat.builder().name("New Chat").build();
        Chat savedChat = Chat.builder().id(chatId).name("New Chat").users(Set.of(user)).build();
        UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getRole(), user.getUsername(),
            user.getEmail());
        ChatResponseDto responseDto = new ChatResponseDto(chatId, "New Chat", Set.of(userResponseDto));

        when(chatMapper.toEntity(requestDto)).thenReturn(newChat);
        when(userRepository.findAllById(requestDto.userIds())).thenReturn(List.of(user));
        when(chatRepository.save(newChat)).thenReturn(savedChat);
        when(chatMapper.toDto(savedChat)).thenReturn(responseDto);

        ChatResponseDto result = chatService.createChat(requestDto);

        assertEquals(chatId, result.id());
        assertEquals("New Chat", result.name());
        assertEquals(1, result.users().size());
        verify(chatRepository).save(newChat);
    }

    @Test
    @DisplayName("addUserToChat должен добавить пользователя в чат")
    void addUserToChat_ShouldAddUser() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        chatService.addUserToChat(chatId, userId);

        assertTrue(chat.getUsers().contains(user));
        verify(chatRepository).findById(chatId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("addUserToChat должен бросать исключение, если чат не найден")
    void addUserToChat_ShouldThrow_WhenChatNotFound() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> chatService.addUserToChat(chatId, userId));
        verify(chatRepository).findById(chatId);
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("addUserToChat должен бросать исключение, если пользователь не найден")
    void addUserToChat_ShouldThrow_WhenUserNotFound() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> chatService.addUserToChat(chatId, userId));
    }

    @Test
    @DisplayName("deleteChat должен удалить чат по id")
    void deleteChat_ShouldDeleteChat() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));

        chatService.deleteChat(chatId);

        verify(chatRepository).delete(chat);
    }

    @Test
    @DisplayName("deleteChat должен бросать исключение, если чат не найден")
    void deleteChat_ShouldThrow_WhenChatNotFound() {
        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> chatService.deleteChat(chatId));
        verify(chatRepository, never()).delete(any());
    }
}
