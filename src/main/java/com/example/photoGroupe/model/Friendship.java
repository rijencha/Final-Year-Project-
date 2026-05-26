package com.example.photoGroupe.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Constructors ──────────────────────────────────────────────────────

    public Friendship() {}

    public Friendship(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = FriendshipStatus.PENDING;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public User getSender()                     { return sender; }
    public User getReceiver()                   { return receiver; }
    public FriendshipStatus getStatus()         { return status; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }

    public void setSender(User sender)          { this.sender = sender; }
    public void setReceiver(User receiver)      { this.receiver = receiver; }
    public void setStatus(FriendshipStatus s)   { this.status = s; }
}
