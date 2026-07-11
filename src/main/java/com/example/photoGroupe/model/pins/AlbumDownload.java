package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "album_downloads")
public class AlbumDownload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    private Album album;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "downloaded_at")
    private LocalDateTime downloadedAt;

    @PrePersist protected void onCreate() { this.downloadedAt = LocalDateTime.now(); }

    public AlbumDownload() {}
    public AlbumDownload(Album album, User user) {
        this.album = album;
        this.user  = user;
    }

    public Long getId()                    { return id; }
    public Album getAlbum()               { return album; }
    public User getUser()                 { return user; }
    public LocalDateTime getDownloadedAt(){ return downloadedAt; }
}
