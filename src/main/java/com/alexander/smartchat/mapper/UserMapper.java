package com.alexander.smartchat.mapper;

import com.alexander.smartchat.dto.UserRequestDto;
import com.alexander.smartchat.dto.UserResponseDto;
import com.alexander.smartchat.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto requestDto);

    UserResponseDto toDto(User user);
}
