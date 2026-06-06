package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "saved_pins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pin_id"}))
public class SavedPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @Column(name = "saved_at", updatable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() { this.savedAt = LocalDateTime.now(); }

    public SavedPin() {}

    public SavedPin(User user, Pin pin) {
        this.user = user;
        this.pin  = pin;
    }

    public Long getId()             { return id; }
    public User getUser()           { return user; }
    public Pin getPin()             { return pin; }
    public LocalDateTime getSavedAt() { return savedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedPin)) return false;
        return Objects.equals(id, ((SavedPin) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
