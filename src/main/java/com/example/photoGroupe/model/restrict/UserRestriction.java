package com.example.photoGroupe.model.restrict;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents "restrictor" limiting what "restricted" can do TOWARD them
 * for a specific RestrictionType. Purely peer-to-peer — no admin involved.
 *
 * One-directional by design: A restricting B does not imply B restricting A.
 * Silent by design: B is never notified this row exists.
 */
@Entity
@Table(
        name = "user_restrictions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_restriction_unique",
                columnNames = {"restrictor_id", "restricted_id", "type"}
        )
)
public class UserRestriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who applied the restriction (the one being protected). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restrictor_id", nullable = false)
    private User restrictor;

    /** The user who is limited by this restriction. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restricted_id", nullable = false)
    private User restricted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionType type;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public UserRestriction() {}

    public UserRestriction(User restrictor, User restricted, RestrictionType type) {
        this.restrictor = restrictor;
        this.restricted = restricted;
        this.type = type;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────

    public Long getId()                 { return id; }
    public User getRestrictor()         { return restrictor; }
    public User getRestricted()         { return restricted; }
    public RestrictionType getType()    { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRestrictor(User restrictor)   { this.restrictor = restrictor; }
    public void setRestricted(User restricted)   { this.restricted = restricted; }
    public void setType(RestrictionType type)    { this.type = type; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRestriction)) return false;
        return Objects.equals(id, ((UserRestriction) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}