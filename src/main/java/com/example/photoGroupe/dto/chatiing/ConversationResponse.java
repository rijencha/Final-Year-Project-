package com.example.photoGroupe.dto.chatiing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ConversationResponse {
    private Long          id;
    private Long          otherUserId;
    private String        otherUsername;
    private String        otherProfilePicture;
    private String        lastMessage;
    private MessageResponse lastMessageObj;
    private LocalDateTime lastMessageAt;
    private long          unreadCount;
}
