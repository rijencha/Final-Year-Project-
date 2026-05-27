package com.example.photoGroupe.service.notification;

import com.example.photoGroupe.dto.notification.NotificationResponse;
import com.example.photoGroupe.model.User;

import java.util.List;

public interface NotificationService {
    void create (User recipient, User sender, String type, String message, String link);
    List<NotificationResponse> getForUser(Long userId);
    void markRead(Long notificationId);
    void markAllRead(Long userId);
    long getUnreadCount(Long userId);
}
