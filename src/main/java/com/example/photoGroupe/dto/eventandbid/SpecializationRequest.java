package com.example.photoGroupe.dto.eventandbid;

import com.example.photoGroupe.model.event.EventType;
import lombok.Data;

@Data
public class SpecializationRequest {
//    private EventType eventType;
    private String customType;
    private String note;
    private Long categoryId;
}
