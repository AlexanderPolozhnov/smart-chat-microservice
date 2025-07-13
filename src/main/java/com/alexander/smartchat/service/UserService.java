package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.UserRequestDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.User;
import com.alexander.smartchat.mapper.UserMapper;
import com.alexander.smartchat.repository.UserRepository;
import com.alexander.smartchat.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Аккаунт не был найден: " + username));

        return new UserDetailsImpl(user);
    }

    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
            .map(userMapper::toDto)
            .collect(Collectors.toList());
    }

    public UserResponseDto getById(UUID id) {
        return userRepository.findById(id)
            .map(userMapper::toDto)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + id + " не был найден"));
    }

    @Transactional
    public UserResponseDto createUser(UserRequestDto requestDto) {
        return Optional.of(requestDto)
            .map(userMapper::toEntity)
            .map(user -> {
                user.setPassword(passwordEncoder.encode(requestDto.password()));
                return user;
            })
            .map(userRepository::save)
            .map(userMapper::toDto)
            .orElseThrow(() -> new RuntimeException("Ошибка при создании пользователя"));
    }

    @Transactional
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с id " + id + " не был найден"));
        userRepository.delete(user);
    }
}
