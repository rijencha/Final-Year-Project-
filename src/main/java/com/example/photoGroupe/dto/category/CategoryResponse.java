package com.example.photoGroupe.dto.category;

import com.example.photoGroupe.model.Category;

import java.time.LocalDateTime;

public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String slug;
    private String coverImage;
    private String createdBy;   // full name of the admin who created it
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CategoryResponse() {}

    /** Factory method – converts entity → DTO */
    public static CategoryResponse from(Category c) {
        CategoryResponse dto = new CategoryResponse();
        dto.id          = c.getId();
        dto.name        = c.getName();
        dto.description = c.getDescription();
        dto.slug        = c.getSlug();
        dto.coverImage  = c.getCoverImage();
        dto.createdAt   = c.getCreatedAt();
        dto.updatedAt   = c.getUpdatedAt();
        dto.createdBy   = c.getCreatedBy() != null ? c.getCreatedBy().getFullName() : null;
        return dto;
    }

    // ─── Getters ──────────────────────────────────────────────────────────

    public Long getId()                  { return id; }
    public String getName()              { return name; }
    public String getDescription()       { return description; }
    public String getSlug()              { return slug; }
    public String getCoverImage()        { return coverImage; }
    public String getCreatedBy()         { return createdBy; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }

    // ─── Setters ──────────────────────────────────────────────────────────

    public void setId(Long id)                              { this.id = id; }
    public void setName(String name)                        { this.name = name; }
    public void setDescription(String description)          { this.description = description; }
    public void setSlug(String slug)                        { this.slug = slug; }
    public void setCoverImage(String coverImage)            { this.coverImage = coverImage; }
    public void setCreatedBy(String createdBy)              { this.createdBy = createdBy; }
    public void setCreatedAt(LocalDateTime createdAt)       { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)       { this.updatedAt = updatedAt; }
}
