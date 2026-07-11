package com.example.photoGroupe.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Records a single (viewer, profileOwner, day) tuple so that repeat views
 * from the same viewer on the same day don't inflate the profile view count.
 * Self-views are never recorded (enforced at the service layer).
 */
@Entity
@Table(
        name = "profile_views",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_profile_view_per_day",
                columnNames = {"viewer_id", "profile_owner_id", "viewed_date"}
        )
)
public class ProfileView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "viewer_id", nullable = false)
    private Long viewerId;

    @Column(name = "profile_owner_id", nullable = false)
    private Long profileOwnerId;

    @Column(name = "viewed_date", nullable = false)
    private LocalDate viewedDate;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        this.viewedAt = LocalDateTime.now();
        if (this.viewedDate == null) {
            this.viewedDate = LocalDate.now();
        }
    }

    public ProfileView() {}

    public ProfileView(Long viewerId, Long profileOwnerId, LocalDate viewedDate) {
        this.viewerId = viewerId;
        this.profileOwnerId = profileOwnerId;
        this.viewedDate = viewedDate;
    }

    public Long getId() { return id; }
    public Long getViewerId() { return viewerId; }
    public Long getProfileOwnerId() { return profileOwnerId; }
    public LocalDate getViewedDate() { return viewedDate; }
    public LocalDateTime getViewedAt() { return viewedAt; }

    public void setId(Long id) { this.id = id; }
    public void setViewerId(Long viewerId) { this.viewerId = viewerId; }
    public void setProfileOwnerId(Long profileOwnerId) { this.profileOwnerId = profileOwnerId; }
    public void setViewedDate(LocalDate viewedDate) { this.viewedDate = viewedDate; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}