package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pins")
public class Pin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Content ──────────────────────────────────────────────────────────

    @Column(nullable = false)
    private String imageUrl;          // Cloudinary secure_url

    @Column(nullable = false)
    private String publicId;          // Cloudinary public_id (for deletion)

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String tags;              // comma-separated, e.g. "sunset,travel,nepal"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Suspend or band pins
    @Column(name = "is_suspended", nullable = false)
    private boolean suspended = false;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suspended_by")
    private User suspendedBy;

    // ─── Relationships ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PinLike> likes = new ArrayList<>();

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── Constructors ─────────────────────────────────────────────────────

    public Pin() {}

    public Pin(String imageUrl, String publicId, String title, String description, String tags, User user) {
        this.imageUrl    = imageUrl;
        this.publicId    = publicId;
        this.title       = title;
        this.description = description;
        this.tags        = tags;
        this.user        = user;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    public int getLikeCount()    { return likes.size(); }
    public int getCommentCount() { return comments.size(); }

    // ─── Getters / Setters ────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getImageUrl()             { return imageUrl; }
    public String getPublicId()             { return publicId; }
    public String getTitle()                { return title; }
    public String getDescription()          { return description; }
    public String getTags()                 { return tags; }
    public User getUser()                   { return user; }
    public List<Comment> getComments()      { return comments; }
    public List<PinLike> getLikes()         { return likes; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public boolean isDeleted()              { return deleted; }
    public Category getCategory()                  { return category; }
    public boolean isSuspended()            { return suspended; }
    public String getSuspensionReason()     { return suspensionReason; }
    public LocalDateTime getSuspendedAt()   { return suspendedAt; }
    public User getSuspendedBy()            { return suspendedBy; }

    // Setters
    public void setSuspended(boolean s)             { this.suspended = s; }
    public void setSuspensionReason(String r)       { this.suspensionReason = r; }
    public void setSuspendedAt(LocalDateTime t)     { this.suspendedAt = t; }
    public void setSuspendedBy(User u)              { this.suspendedBy = u; }
    public void setCategory(Category category)     { this.category = category; }
    public void setImageUrl(String imageUrl)        { this.imageUrl = imageUrl; }
    public void setPublicId(String publicId)         { this.publicId = publicId; }
    public void setTitle(String title)               { this.title = title; }
    public void setDescription(String description)   { this.description = description; }
    public void setTags(String tags)                 { this.tags = tags; }
    public void setUser(User user)                   { this.user = user; }
    public void setDeleted(boolean deleted)          { this.deleted = deleted; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pin)) return false;
        return Objects.equals(id, ((Pin) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}