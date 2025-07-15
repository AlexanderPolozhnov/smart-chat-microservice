package com.alexander.smartchat.mapper;

import com.alexander.smartchat.dto.ChatRequestDto;
import com.alexander.smartchat.dto.ChatResponseDto;
import com.alexander.smartchat.entity.Chat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    Chat toEntity(ChatRequestDto requestDto);

    ChatResponseDto toDto(Chat chat);
}
