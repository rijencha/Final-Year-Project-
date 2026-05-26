package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    // ─── Relationships ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    // ─── Reply support ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    // ─── Likes ────────────────────────────────────────────────────────────

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedBy = new HashSet<>();

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ─── Constructors ─────────────────────────────────────────────────────

    public Comment() {}

    public Comment(String text, User user, Pin pin) {
        this.text = text;
        this.user = user;
        this.pin  = pin;
    }

    public Comment(String text, User user, Pin pin, Comment parent) {
        this.text   = text;
        this.user   = user;
        this.pin    = pin;
        this.parent = parent;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────

    public Long getId()                   { return id; }
    public String getText()               { return text; }
    public User getUser()                 { return user; }
    public Pin getPin()                   { return pin; }
    public Comment getParent()            { return parent; }
    public List<Comment> getReplies()     { return replies; }
    public Set<User> getLikedBy()         { return likedBy; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
    public boolean isDeleted()            { return deleted; }

    public void setText(String text)          { this.text = text; }
    public void setUser(User user)            { this.user = user; }
    public void setPin(Pin pin)               { this.pin = pin; }
    public void setParent(Comment parent)     { this.parent = parent; }
    public void setDeleted(boolean deleted)   { this.deleted = deleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Comment)) return false;
        return Objects.equals(id, ((Comment) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}