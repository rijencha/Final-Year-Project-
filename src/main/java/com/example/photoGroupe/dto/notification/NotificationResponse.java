package com.example.photoGroupe.dto.notification;

import com.example.photoGroupe.model.Notification;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class NotificationResponse {
    private Long   id;
    private String type;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
    private Long   senderId;
    private String senderName;
    private String senderAvatar;

    public NotificationResponse(Notification n) {
        this.id          = n.getId();
        this.type        = n.getType();
        this.message     = n.getMessage();
        this.link        = n.getLink();
        this.read        = n.isRead();
        this.createdAt   = n.getCreatedAt();

        if (n.getSender() != null) {
            this.senderId     = n.getSender().getId();
            this.senderName   = n.getSender().getFullName();
            this.senderAvatar = n.getSender().getProfilePicture();
        }
    }
}