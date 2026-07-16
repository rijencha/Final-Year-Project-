package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Per-user weight for one category, used to bias feed sampling.
 * weight == 1.0 is neutral/default (no row needed — absence means 1.0).
 * weight > 1.0 means "see more", < 1.0 means "see less".
 * This is a soft signal — unlike FeedExclusion, nothing is ever fully hidden here.
 */
@Entity
@Table(
        name = "category_preferences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_category_preference_unique",
                columnNames = {"user_id", "category_id"}
        )
)
public class CategoryPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private double weight = 1.0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() { this.updatedAt = LocalDateTime.now(); }

    public CategoryPreference() {}

    public CategoryPreference(User user, Category category, double weight) {
        this.user = user;
        this.category = category;
        this.weight = weight;
    }

    public Long getId()                 { return id; }
    public User getUser()               { return user; }
    public Category getCategory()       { return category; }
    public double getWeight()           { return weight; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setWeight(double weight) { this.weight = weight; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryPreference)) return false;
        return Objects.equals(id, ((CategoryPreference) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}