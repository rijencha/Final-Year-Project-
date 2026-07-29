package com.example.photoGroupe.model.chatting;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.pins.Album;
import com.example.photoGroupe.model.workshop.Workshop;
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

    @Column(name = "booking_package_id")
    private Long bookingPackageId;

    // Optional: shared pin
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_pin_id")
    private Pin sharedPin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_workshop_id")
    private Workshop sharedWorkshop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_album_id")
    private Album sharedAlbum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_profile_id")
    private User sharedProfile;

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
    public Long getBookingPackageId()           { return bookingPackageId; }


    public void setBookingPackageId(Long id)    { this.bookingPackageId = id; }

    public Workshop getSharedWorkshop()      { return sharedWorkshop; }
    public void setSharedWorkshop(Workshop w) { this.sharedWorkshop = w; }

    public Album getSharedAlbum()            { return sharedAlbum; }
    public void setSharedAlbum(Album a)      { this.sharedAlbum = a; }

    public User getSharedProfile()           { return sharedProfile; }
    public void setSharedProfile(User u)     { this.sharedProfile = u; }

    // Setters
    public void setConversation(Conversation c) { this.conversation = c; }
    public void setSender(User sender)          { this.sender = sender; }
    public void setText(String text)            { this.text = text; }
    public void setSharedPin(Pin pin)           { this.sharedPin = pin; }
    public void setType(MessageType type)       { this.type = type; }
    public void setRead(boolean read)           { this.read = read; }
    public void setDeleted(boolean deleted)     { this.deleted = deleted; }
}