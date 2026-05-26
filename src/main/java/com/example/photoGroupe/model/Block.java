package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "blocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_blocker_blocked",
                columnNames = {"blocker_id", "blocked_id"}
        ),
        indexes = {
                @Index(name = "idx_blocker_id", columnList = "blocker_id"),
                @Index(name = "idx_blocked_id", columnList = "blocked_id")
        }
)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user doing the blocking */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    /** The user being blocked */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Constructors ─────────────────────────────────────────────────────

    public Block() {}

    public Block(User blocker, User blocked) {
        this.blocker = blocker;
        this.blocked = blocked;
    }

    // ─── Getters ──────────────────────────────────────────────────────────

    public Long getId()                 { return id; }
    public User getBlocker()            { return blocker; }
    public User getBlocked()            { return blocked; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── equals / hashCode ────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Block)) return false;
        Block b = (Block) o;
        return Objects.equals(blocker, b.blocker) &&
                Objects.equals(blocked, b.blocked);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocker, blocked);
    }

    @Override
    public String toString() {
        return "Block{blocker=" + blocker.getId() +
                ", blocked=" + blocked.getId() + "}";
    }
}