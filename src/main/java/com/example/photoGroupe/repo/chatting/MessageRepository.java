package com.example.photoGroupe.repo.chatting;

import com.example.photoGroupe.model.chatting.Conversation;
import com.example.photoGroupe.model.chatting.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(
            Long conversationId, Pageable pageable);

    List<Message> findByConversationIdAndDeletedFalseAndCreatedAtAfterOrderByCreatedAtAsc(
            Long conversationId, LocalDateTime after);

    long countByConversationIdAndReadFalseAndSenderIdNot(Long conversationId, Long senderId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Message m SET m.read = true
        WHERE m.conversation.id = :conversationId
        AND m.sender.id != :readerId
        AND m.read = false
    """)
    void markAllAsRead(Long conversationId, Long readerId);

    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.conversation.id IN (
            SELECT c.id FROM Conversation c
            WHERE c.userOne.id = :userId OR c.userTwo.id = :userId
        )
        AND m.sender.id != :userId
        AND m.read = false
        AND m.deleted = false
    """)
    long countTotalUnread(Long userId);

}
