package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Core Fields ──────────────────────────────────────────────────────

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    /** URL-friendly slug, e.g. "landscape-photography" */
    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "public_id")
    private String publicId;

    /** Optional cover image stored as a path/URL */
    @Column(name = "cover_image")
    private String coverImage;

    // ─── Soft Delete ──────────────────────────────────────────────────────

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Audit ────────────────────────────────────────────────────────────

    /** The admin/super-admin who created this category */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    // ─── Lifecycle ────────────────────────────────────────────────────────

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

    public Category() {}

    public Category(String name, String description, String slug, User createdBy) {
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.createdBy = createdBy;
    }

    // ─── Getters ──────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getName()                 { return name; }
    public String getDescription()          { return description; }
    public String getSlug()                 { return slug; }
    public String getCoverImage()           { return coverImage; }
    public boolean isDeleted()              { return deleted; }
    public LocalDateTime getDeletedAt()     { return deletedAt; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public User getCreatedBy()              { return createdBy; }
    public String getPublicId()              { return publicId; }

       // ─── Setters ──────────────────────────────────────────────────────────

    public void setId(Long id)                              { this.id = id; }
    public void setName(String name)                        { this.name = name; }
    public void setDescription(String description)          { this.description = description; }
    public void setSlug(String slug)                        { this.slug = slug; }
    public void setCoverImage(String coverImage)            { this.coverImage = coverImage; }
    public void setDeleted(boolean deleted)                 { this.deleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt)       { this.deletedAt = deletedAt; }
    public void setCreatedAt(LocalDateTime createdAt)       { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)       { this.updatedAt = updatedAt; }
    public void setCreatedBy(User createdBy)                { this.createdBy = createdBy; }
    public void setPublicId(String publicId) { this.publicId = publicId; }

    // ─── equals / hashCode / toString ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;
        Category that = (Category) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "', slug='" + slug + "'}";
    }
}
