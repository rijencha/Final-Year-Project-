package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.event.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingRequest {
    @NotNull
    private Long photographerId;
    @NotBlank
    private String eventTitle;
    @NotNull
    private EventType eventType;
    @NotNull
    private LocalDateTime eventDate;
    @NotBlank
    private String location;
    private String description;
    private Integer durationHours;
    @NotNull
    private BigDecimal price;
    private String specialRequests;

    // getters & setters for all
    public Long getPhotographerId()       { return photographerId; }
    public String getEventTitle()         { return eventTitle; }
    public EventType getEventType()       { return eventType; }
    public LocalDateTime getEventDate()   { return eventDate; }
    public String getLocation()           { return location; }
    public String getDescription()        { return description; }
    public Integer getDurationHours()     { return durationHours; }
    public BigDecimal getPrice()          { return price; }
    public String getSpecialRequests()    { return specialRequests; }
}
