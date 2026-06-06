package com.example.photoGroupe.model.rating;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(
        name = "photographer_reviews",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"reviewer_id", "photographer_id"},
                name = "uq_one_review_per_photographer"
        )
)
public class PhotographerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Rating (1–5) ─────────────────────────────────────────────────────

    @Column(nullable = false)
    private int rating; // 1 to 5

    // ─── Review Text ──────────────────────────────────────────────────────

    @Column(columnDefinition = "TEXT")
    private String comment;

    // ─── Relationships ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    // ─── Soft Delete ──────────────────────────────────────────────────────

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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

    public PhotographerReview() {}

    public PhotographerReview(int rating, String comment, User reviewer, User photographer) {
        this.rating = rating;
        this.comment = comment;
        this.reviewer = reviewer;
        this.photographer = photographer;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────

    public Long getId()                      { return id; }
    public int getRating()                   { return rating; }
    public String getComment()               { return comment; }
    public User getReviewer()                { return reviewer; }
    public User getPhotographer()            { return photographer; }
    public boolean isDeleted()               { return deleted; }
    public LocalDateTime getCreatedAt()      { return createdAt; }
    public LocalDateTime getUpdatedAt()      { return updatedAt; }

    public void setRating(int rating)        { this.rating = rating; }
    public void setComment(String comment)   { this.comment = comment; }
    public void setDeleted(boolean deleted)  { this.deleted = deleted; }

    // ─── equals / hashCode ────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotographerReview)) return false;
        return Objects.equals(id, ((PhotographerReview) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
