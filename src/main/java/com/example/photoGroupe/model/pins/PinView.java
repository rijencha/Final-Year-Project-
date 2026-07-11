package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pin_views", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"pin_id", "user_id"})
})
public class PinView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "last_viewed_at", nullable = false)
    private LocalDateTime lastViewedAt;

    public PinView() {}

    public PinView(Pin pin, User user) {
        this.pin = pin;
        this.user = user;
        this.lastViewedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId()                     { return id; }
    public Pin getPin()                     { return pin; }
    public User getUser()                   { return user; }
    public LocalDateTime getLastViewedAt()  { return lastViewedAt; }
    public void setLastViewedAt(LocalDateTime t) { this.lastViewedAt = t; }
}