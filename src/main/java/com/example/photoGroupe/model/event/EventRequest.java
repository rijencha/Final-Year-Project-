package com.example.photoGroupe.model.event;

import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// model/EventRequest.java
@Entity
@Table(name = "event_requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private String title;               // "John's Wedding"

    @Enumerated(EnumType.STRING)
    @Column
    private EventType eventType;        // WEDDING, BIRTHDAY, CORPORATE, etc.

    @Column(name = "custom_event_type")
    private String customEventType;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String location;

    private String description;

    @Column(nullable = false)
    private BigDecimal budgetMin;

    @Column(nullable = false)
    private BigDecimal budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRequestStatus status;  // OPEN, CLOSED, CANCELLED, COMPLETED

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;   // bidding deadline

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "eventRequest", cascade = CascadeType.ALL)
    private List<Bid> bids = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status    = EventRequestStatus.OPEN;
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId()                         { return id; }
    public User getClient()                     { return client; }
    public String getTitle()                    { return title; }
    public EventType getEventType()             { return eventType; }
    public LocalDateTime getEventDate()         { return eventDate; }
    public String getLocation()                 { return location; }
    public String getDescription()              { return description; }
    public BigDecimal getBudgetMin()            { return budgetMin; }
    public BigDecimal getBudgetMax()            { return budgetMax; }
    public EventRequestStatus getStatus()       { return status; }
    public LocalDateTime getDeadlineAt()        { return deadlineAt; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
    public List<Bid> getBids()                  { return bids; }
    public void setStatus(EventRequestStatus s) { this.status = s; }
    public void setUpdatedAt(LocalDateTime t)   { this.updatedAt = t; }
    public String getCustomEventType() { return customEventType; }
}