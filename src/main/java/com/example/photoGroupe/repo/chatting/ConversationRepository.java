package com.example.photoGroupe.repo.chatting;

import com.example.photoGroupe.model.chatting.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
        SELECT c FROM Conversation c
        WHERE (c.userOne.id = :userOneId AND c.userTwo.id = :userTwoId)
           OR (c.userOne.id = :userTwoId AND c.userTwo.id = :userOneId)
    """)
    Optional<Conversation> findBetweenUsers(Long userOneId, Long userTwoId);

    // All conversations for a user, newest first
    @Query("""
        SELECT c FROM Conversation c
        WHERE c.userOne.id = :userId OR c.userTwo.id = :userId
        ORDER BY c.updatedAt DESC
    """)
    List<Conversation> findAllByUserId(Long userId);
}
