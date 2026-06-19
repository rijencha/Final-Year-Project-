package com.example.photoGroupe.dto.eventandbid;

import com.example.photoGroupe.model.event.EventType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class EventRequestDTO {
    private String title;
    private EventType eventType;
    private String customEventType;
    private LocalDateTime eventDate;
    private String location;
    private String description;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private LocalDateTime deadlineAt;
    // getters & setters for all fields
}
