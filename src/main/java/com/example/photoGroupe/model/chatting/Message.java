package com.example.photoGroupe.model.chatting;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.awt.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(columnDefinition = "TEXT")
    private String text;

    // Optional: shared pin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_pin_id")
    private Pin sharedPin;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Message() {}

    public Message(Conversation conversation, User sender, String text, MessageType type) {
        this.conversation = conversation;
        this.sender       = sender;
        this.text         = text;
        this.type         = type;
    }

    // Getters
    public Long getId()                   { return id; }
    public Conversation getConversation() { return conversation; }
    public User getSender()               { return sender; }
    public String getText()               { return text; }
    public Pin getSharedPin()             { return sharedPin; }
    public MessageType getType()          { return type; }
    public boolean isRead()               { return read; }
    public boolean isDeleted()            { return deleted; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Setters
    public void setConversation(Conversation c) { this.conversation = c; }
    public void setSender(User sender)          { this.sender = sender; }
    public void setText(String text)            { this.text = text; }
    public void setSharedPin(Pin pin)           { this.sharedPin = pin; }
    public void setType(MessageType type)       { this.type = type; }
    public void setRead(boolean read)           { this.read = read; }
    public void setDeleted(boolean deleted)     { this.deleted = deleted; }
}