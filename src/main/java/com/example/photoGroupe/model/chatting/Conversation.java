package com.example.photoGroupe.model.chatting;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The two participants
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

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
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Conversation() {}

    public Conversation(User userOne, User userTwo) {
        this.userOne = userOne;
        this.userTwo = userTwo;
    }

    // Getters
    public Long getId()                     { return id; }
    public User getUserOne()                { return userOne; }
    public User getUserTwo()                { return userTwo; }
    public List<Message> getMessages()      { return messages; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    // Setters
    public void setUserOne(User userOne)    { this.userOne = userOne; }
    public void setUserTwo(User userTwo)    { this.userTwo = userTwo; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }

    // Helper: get the other participant
    public User getOtherUser(Long currentUserId) {
        return userOne.getId().equals(currentUserId) ? userTwo : userOne;
    }
}