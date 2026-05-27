package com.example.photoGroupe.service.notification;

import com.example.photoGroupe.dto.notification.NotificationResponse;
import com.example.photoGroupe.model.Notification;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{

    private final NotificationRepository notificationRepository;

    @Override
    public void create(User recipient, User sender, String type, String message, String link) {
        if(sender != null && sender.getId().equals(recipient.getId())) return;

        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .type(type)
                .message(message)
                .link(link)
                .read(false)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    @Override
    public void markRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Override
    public void markAllRead(Long userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }
}
