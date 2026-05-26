package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "follows",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_follower_following",
                columnNames = {"follower_id", "following_id"}
        ),
        indexes = {
                @Index(name = "idx_follower_id",  columnList = "follower_id"),
                @Index(name = "idx_following_id", columnList = "following_id")
        }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who is doing the following */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    /** The user being followed */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "following_id", nullable = false)
    private User following;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ─────────────────────────────────────────────────────

    public Follow() {}

    public Follow(User follower, User following) {
        this.follower  = follower;
        this.following = following;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public User getFollower()            { return follower; }
    public User getFollowing()           { return following; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    public void setFollower(User follower)   { this.follower  = follower; }
    public void setFollowing(User following) { this.following = following; }

    // ─── equals / hashCode ────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Follow)) return false;
        Follow f = (Follow) o;
        return Objects.equals(follower,  f.follower) &&
                Objects.equals(following, f.following);
    }

    @Override
    public int hashCode() {
        return Objects.hash(follower, following);
    }

    @Override
    public String toString() {
        return "Follow{follower=" + follower.getId() +
                ", following=" + following.getId() + "}";
    }
}