package com.example.photoGroupe.dto.eventandbid;

import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.model.event.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class EventRequestResponse {
    private String message;
    private Long id;
    private String title;
    private EventType eventType;
    private LocalDateTime eventDate;
    private String location;
    private String description;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private EventRequestStatus status;
    private LocalDateTime deadlineAt;
    private LocalDateTime createdAt;
    private int bidCount;

    // client info
    private Long clientId;
    private String clientName;
    private String clientAvatar;

    public EventRequestResponse(EventRequest e) {
        this.id          = e.getId();
        this.title       = e.getTitle();
        this.eventType   = e.getEventType();
        this.eventDate   = e.getEventDate();
        this.location    = e.getLocation();
        this.description = e.getDescription();
        this.budgetMin   = e.getBudgetMin();
        this.budgetMax   = e.getBudgetMax();
        this.status      = e.getStatus();
        this.deadlineAt  = e.getDeadlineAt();
        this.createdAt   = e.getCreatedAt();
        this.bidCount    = e.getBids().size();
        this.clientId    = e.getClient().getId();
        this.clientName  = e.getClient().getFullName();
        this.clientAvatar = e.getClient().getProfilePicture();
    }
    // getters
    public EventRequestResponse(EventRequest e, String message) {
        this(e);
        this.message = message;
    }

    // All getters
    public String getMessage()              { return message; }
    public Long getId()                     { return id; }
    public String getTitle()                { return title; }
    public EventType getEventType()         { return eventType; }
    public LocalDateTime getEventDate()     { return eventDate; }
    public String getLocation()             { return location; }
    public String getDescription()          { return description; }
    public BigDecimal getBudgetMin()        { return budgetMin; }
    public BigDecimal getBudgetMax()        { return budgetMax; }
    public EventRequestStatus getStatus()   { return status; }
    public LocalDateTime getDeadlineAt()    { return deadlineAt; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public int getBidCount()                { return bidCount; }
    public Long getClientId()               { return clientId; }
    public String getClientName()           { return clientName; }
    public String getClientAvatar()         { return clientAvatar; }
}