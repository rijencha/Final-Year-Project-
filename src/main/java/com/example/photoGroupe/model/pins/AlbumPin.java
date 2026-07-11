package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Pin;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_pins",
        uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "pin_id"}))
public class AlbumPin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    @PrePersist protected void onCreate() { this.addedAt = LocalDateTime.now(); }

    public AlbumPin() {}
    public AlbumPin(Album album, Pin pin) {
        this.album = album;
        this.pin   = pin;
    }

    public Long getId()          { return id; }
    public Album getAlbum()      { return album; }
    public Pin getPin()          { return pin; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
