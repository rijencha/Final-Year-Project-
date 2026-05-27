package com.example.photoGroupe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// model/Notification.java
@Entity
@Table(name = "notifications")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;       // who receives it

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;          // who triggered it

    @Column(nullable = false)
    private String type;          // "LIKE", "COMMENT", "FOLLOW", "BOOKING"

    @Column(nullable = false)
    private String message;       // "John liked your pin"

    private String link;          // "/pin/42"

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId()                   { return id; }
    public User getRecipient()            { return recipient; }
    public User getSender()               { return sender; }
    public String getType()               { return type; }
    public String getMessage()            { return message; }
    public String getLink()               { return link; }
    public boolean isRead()               { return read; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public void setRead(boolean read)     { this.read = read; }
}
