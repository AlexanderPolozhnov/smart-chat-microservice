package com.alexander.smartchat.service;

import com.alexander.smartchat.dto.MessageResponseDto;
import com.alexander.smartchat.dto.MessageSearchDto;
import com.alexander.smartchat.dto.MessageStatsDto;
import com.alexander.smartchat.mapper.MessageMapper;
import com.alexander.smartchat.repository.MessageRepository;
import jakarta.persistence.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository repository;

    @Mock
    private MessageMapper mapper;

    @InjectMocks
    private MessageService service;

    private UUID chatId;
    private Pageable pageable;
    private Sort sortDesc;

    @BeforeEach
    void setUp() {
        chatId = UUID.randomUUID();
        sortDesc = Sort.by("sentAt").descending();
    }

    @Test
    @DisplayName("getMessages должен вернуть маппированный список сообщений")
    void getMessages_ShouldReturnMappedList() {
        int limit = 2;
        pageable = PageRequest.of(0, limit, sortDesc);

        var now = Instant.now();
        var msg1 = new com.alexander.smartchat.entity.ChatMessage();
        msg1.setSentAt(now);
        var dto1 = new MessageResponseDto(UUID.randomUUID(), chatId, UUID.randomUUID(), "hello", now);

        when(repository.findByChatIdOrderBySentAtDesc(chatId, pageable)).thenReturn(List.of(msg1));
        when(mapper.toDto(msg1)).thenReturn(dto1);

        var result = service.getMessages(chatId, limit);

        assertThat(result).containsExactly(dto1);
        verify(repository).findByChatIdOrderBySentAtDesc(chatId, pageable);
        verify(mapper).toDto(msg1);
    }

    @Test
    @DisplayName("search должен возвращать результаты и общее число в DTO")
    void search_ShouldReturnSearchDto() {
        int limit = 3;
        pageable = PageRequest.of(0, limit, sortDesc);
        String keyword = "test";

        var msg = new com.alexander.smartchat.entity.ChatMessage();
        var dto = new MessageResponseDto(UUID.randomUUID(), chatId, UUID.randomUUID(), "containsTest", Instant.now());
        when(repository.findByChatIdAndTextContainingIgnoreCase(chatId, keyword, pageable))
            .thenReturn(List.of(msg));
        when(mapper.toDto(msg)).thenReturn(dto);
        when(repository.countByChatIdAndTextContainingIgnoreCase(chatId, keyword)).thenReturn(5L);

        MessageSearchDto result = service.search(chatId, keyword, limit);

        assertThat(result.messages()).containsExactly(dto);
        assertThat(result.totalMatches()).isEqualTo(5);
        verify(repository).findByChatIdAndTextContainingIgnoreCase(chatId, keyword, pageable);
        verify(repository).countByChatIdAndTextContainingIgnoreCase(chatId, keyword);
    }

    @Test
    @DisplayName("getStats должен возвращать общие и по пользователям")
    void getStats_ShouldReturnStatsDto() {
        when(repository.countByChatId(chatId)).thenReturn(10L);

        var tuple = mock(Tuple.class);
        UUID user1 = UUID.randomUUID();
        when(tuple.get("sender", UUID.class)).thenReturn(user1);
        when(tuple.get("count", Long.class)).thenReturn(7L);
        when(repository.countPerUserInChat(chatId)).thenReturn(List.of(tuple));

        MessageStatsDto stats = service.getStats(chatId);

        assertThat(stats.totalMessages()).isEqualTo(10L);
        assertThat(stats.messagesPerUser()).containsEntry(user1, 7L);
        verify(repository).countByChatId(chatId);
        verify(repository).countPerUserInChat(chatId);
    }
}
