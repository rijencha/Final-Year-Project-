package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n " +
            "LEFT JOIN FETCH n.sender " +
            "LEFT JOIN FETCH n.recipient " +
            "WHERE n.recipient.id = :userId " +
            "ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.id = :userId")
    void markAllReadByUserId(@Param("userId") Long userId);

    long countByRecipientIdAndReadFalse(Long recipientId);
}
