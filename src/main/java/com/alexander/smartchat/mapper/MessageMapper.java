package com.alexander.smartchat.mapper;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.entity.ChatMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "chat.id", target = "chatId")
    @Mapping(source = "sender.id", target = "senderId")
    MessageResponseDto toDto(ChatMessage message);
}
