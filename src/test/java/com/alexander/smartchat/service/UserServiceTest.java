package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.UserRequestDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.Role;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.exception.ResourceNotFoundException;
import com.alexander.smartchat.mapper.UserMapper;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User userEntity;
    private UserRequestDto userRequestDto;
    private UserResponseDto userResponseDto;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        userEntity = User.builder()
            .id(userId)
            .username("testuser")
            .password("encodedPassword")
            .build();

        userRequestDto = new UserRequestDto("testuser", "password123", "email@example.com");

        userResponseDto = new UserResponseDto(userId, Role.USER, "testuser", "email@example.com");
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(userEntity.getUsername(), userDetails.getUsername());
        assertInstanceOf(UserDetailsImpl.class, userDetails);

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername("unknown"));

        verify(userRepository).findByUsername("unknown");
    }

    @Test
    void getAll_ReturnsListOfUserResponseDto() {
        List<User> users = List.of(userEntity);
        List<UserResponseDto> dtos = List.of(userResponseDto);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDto);

        List<UserResponseDto> result = userService.getAll();

        assertEquals(dtos, result);
        verify(userRepository).findAll();
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void getById_UserExists_ReturnsUserResponseDto() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getById(userId);

        assertEquals(userResponseDto, result);
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void getById_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(userId));

        verify(userRepository).findById(userId);
    }

    @Test
    void createUser_Success_ReturnsUserResponseDto() {
        when(userMapper.toEntity(userRequestDto)).thenReturn(userEntity);
        when(passwordEncoder.encode(userRequestDto.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toDto(userEntity)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userRequestDto);

        assertEquals(userResponseDto, result);
        verify(userMapper).toEntity(userRequestDto);
        verify(passwordEncoder).encode(userRequestDto.password());
        verify(userRepository).save(userEntity);
        verify(userMapper).toDto(userEntity);
    }

    @Test
    void deleteUser_UserExists_DeletesUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(userEntity);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any());
    }
}
