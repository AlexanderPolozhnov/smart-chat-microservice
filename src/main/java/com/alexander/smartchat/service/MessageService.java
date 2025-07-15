package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.dto.MessageSearchDto;
import com.alexander.smartchat.dto.MessageStatsDto;
import com.alexander.smartchat.mapper.MessageMapper;
import com.alexander.smartchat.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public List<MessageResponseDto> getMessages(UUID chatId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("sentAt").descending());
        return messageRepository.findByChatIdOrderBySentAtDesc(chatId, pageable)
            .stream()
            .map(messageMapper::toDto)
            .toList();
    }

    public MessageSearchDto search(UUID chatId, String keyword, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("sentAt").descending());
        List<MessageResponseDto> messageResponseDtos = messageRepository
            .findByChatIdAndTextContainingIgnoreCase(chatId, keyword, pageable)
            .stream()
            .map(messageMapper::toDto)
            .toList();

        Long total = messageRepository.countByChatIdAndTextContainingIgnoreCase(chatId, keyword);
        return new MessageSearchDto(messageResponseDtos, total);
    }

    public MessageStatsDto getStats(UUID chatId) {
        Long total = messageRepository.countByChatId(chatId);
        Map<UUID, Long> perUser = messageRepository.countPerUserInChat(chatId).stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get("sender", UUID.class),
                tuple -> tuple.get("count", Long.class)
            ));
        return new MessageStatsDto(total, perUser);
    }
}
