package com.example.photoGroupe.dto.chatiing;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;           // "CHAT", "READ", "TYPING"
    private Object payload;
}
