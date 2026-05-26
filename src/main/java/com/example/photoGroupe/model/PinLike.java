package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "pin_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pin_id"}) // one like per user per pin
)
public class PinLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public PinLike() {}

    public PinLike(User user, Pin pin) {
        this.user = user;
        this.pin  = pin;
    }

    public Long getId()                 { return id; }
    public User getUser()               { return user; }
    public Pin getPin()                 { return pin; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setUser(User user) { this.user = user; }
    public void setPin(Pin pin)    { this.pin = pin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PinLike)) return false;
        return Objects.equals(id, ((PinLike) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}