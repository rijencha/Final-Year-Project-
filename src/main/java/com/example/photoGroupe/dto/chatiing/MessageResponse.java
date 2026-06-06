package com.example.photoGroupe.dto.chatiing;

import com.example.photoGroupe.model.chatting.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class MessageResponse {
    private Long          id;
    private Long          conversationId;
    private Long          senderId;
    private String        senderUsername;
    private String        senderProfilePicture;
    private String        text;
    private MessageType type;
    private boolean       read;
    private LocalDateTime createdAt;

    // Populated when type = PIN_SHARE
    private Long   sharedPinId;
    private String sharedPinImageUrl;
    private String sharedPinTitle;

    private String imageUrl;
    private String imageTitle;
}
