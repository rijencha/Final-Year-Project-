package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pin_downloads")
public class PinDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // nullable — allows anonymous tracking

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;

    @PrePersist protected void onCreate() { this.downloadedAt = LocalDateTime.now(); }

    public PinDownload() {}
    public PinDownload(Pin pin, User user) {
        this.pin  = pin;
        this.user = user;
    }

    public Long getId()                   { return id; }
    public Pin getPin()                   { return pin; }
    public User getUser()                 { return user; }
    public LocalDateTime getDownloadedAt(){ return downloadedAt; }
}
