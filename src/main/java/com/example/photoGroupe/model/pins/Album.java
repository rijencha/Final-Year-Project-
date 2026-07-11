package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // "PUBLIC" | "PRIVATE"
    @Column(nullable = false)
    private String visibility = "PUBLIC";

    // Cloudinary URL for the album cover (auto-set to first pin's image)
    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "cover_public_id")
    private String coverPublicId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AlbumPin> albumPins = new ArrayList<>();

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Album() {}
    public Album(String title, String description, String visibility, User user) {
        this.title       = title;
        this.description = description;
        this.visibility  = visibility;
        this.user        = user;
    }

    // Getters / Setters
    public Long getId()                   { return id; }
    public String getTitle()              { return title; }
    public String getDescription()        { return description; }
    public String getVisibility()         { return visibility; }
    public String getCoverImageUrl()      { return coverImageUrl; }
    public User getUser()                 { return user; }
    public List<AlbumPin> getAlbumPins()  { return albumPins; }
    public boolean isDeleted()            { return deleted; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }
    public String getCoverPublicId()           { return coverPublicId; }
    public long getViewCount()              { return viewCount; }

    public void setViewCount(long viewCount){ this.viewCount = viewCount; }
    public void setCoverPublicId(String id)    { this.coverPublicId = id; }
    public void setTitle(String t)             { this.title = t; }
    public void setDescription(String d)       { this.description = d; }
    public void setVisibility(String v)        { this.visibility = v; }
    public void setCoverImageUrl(String u)     { this.coverImageUrl = u; }
    public void setUser(User u)                { this.user = u; }
    public void setDeleted(boolean d)          { this.deleted = d; }

    public void incrementViewCount()        { this.viewCount++; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;
        return Objects.equals(id, ((Album) o).id);
    }
    @Override public int hashCode() { return Objects.hash(id); }
}
