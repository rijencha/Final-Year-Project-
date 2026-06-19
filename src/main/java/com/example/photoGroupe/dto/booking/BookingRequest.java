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

    private EventType eventType;      // remove @NotNull — optional when customEventType is set

    private String customEventType;

    @NotNull
    private LocalDateTime eventDate;

    @NotBlank
    private String location;

    private String description;
    private Integer durationHours;

    @NotNull
    private BigDecimal price;

    private String specialRequests;

    // ── Getters ──────────────────────────────────────────────────────────
    public Long getPhotographerId()       { return photographerId; }
    public String getEventTitle()         { return eventTitle; }
    public EventType getEventType()       { return eventType; }
    public String getCustomEventType()    { return customEventType; }
    public LocalDateTime getEventDate()   { return eventDate; }
    public String getLocation()           { return location; }
    public String getDescription()        { return description; }
    public Integer getDurationHours()     { return durationHours; }
    public BigDecimal getPrice()          { return price; }
    public String getSpecialRequests()    { return specialRequests; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setPhotographerId(Long photographerId)         { this.photographerId = photographerId; }
    public void setEventTitle(String eventTitle)               { this.eventTitle = eventTitle; }
    public void setEventType(EventType eventType)              { this.eventType = eventType; }
    public void setCustomEventType(String customEventType)     { this.customEventType = customEventType; }
    public void setEventDate(LocalDateTime eventDate)          { this.eventDate = eventDate; }
    public void setLocation(String location)                   { this.location = location; }
    public void setDescription(String description)             { this.description = description; }
    public void setDurationHours(Integer durationHours)        { this.durationHours = durationHours; }
    public void setPrice(BigDecimal price)                     { this.price = price; }
    public void setSpecialRequests(String specialRequests)     { this.specialRequests = specialRequests; }
}
