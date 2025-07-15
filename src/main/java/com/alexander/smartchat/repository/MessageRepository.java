package com.alexander.smartchat.repository;

import com.alexander.smartchat.entity.ChatMessage;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByChatIdOrderBySentAtDesc(UUID chatId, Pageable pg);

    Long countByChatId(UUID chatId);

    @Query("SELECT m.sender.id AS sender, COUNT(m) AS count "
        + "FROM ChatMessage m "
        + "WHERE m.chat.id = :chatId "
        + "GROUP BY m.sender.id")
    List<Tuple> countPerUserInChat(@Param("chatId") UUID chatId);

    List<ChatMessage> findByChatIdAndTextContainingIgnoreCase(UUID chatId, String text, Pageable pg);

    Long countByChatIdAndTextContainingIgnoreCase(UUID chatId, String text);
}
