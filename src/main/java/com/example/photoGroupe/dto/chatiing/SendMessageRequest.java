package com.example.photoGroupe.dto.chatiing;

import com.example.photoGroupe.model.chatting.MessageType;
import lombok.Data;

@Data
public class SendMessageRequest {
    private Long receiverId;       // who to send to
    private String text;           // message text (null if PIN_SHARE)
    private Long sharedPinId;      // optional: pin to share
    private MessageType type = MessageType.TEXT;
    private Long bookingPackageId;
}
