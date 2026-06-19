package com.example.photoGroupe.model.workshop;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workshops")
public class Workshop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Core Details ─────────────────────────────────────────────────────

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "workshop_date", nullable = false)
    private LocalDateTime workshopDate;

    @Column(nullable = false)
    private String location;

    /** Duration label e.g. "4 hours", "2 days" */
    @Column(nullable = false)
    private String duration;

    // ─── Capacity & Pricing ───────────────────────────────────────────────

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "seats_booked", nullable = false)
    private int seatsBooked = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // ─── Media ────────────────────────────────────────────────────────────

    /** Stored path / URL of the cover image */
    @Column(name = "cover_image")
    private String coverImage;

    // ─── Status ───────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkshopStatus status = WorkshopStatus.UPCOMING;

    // ─── Relationships ────────────────────────────────────────────────────

    /** The photographer who hosts this workshop */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @OneToMany(mappedBy = "workshop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkshopParticipant> participants = new ArrayList<>();

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

    // ─── Helpers ──────────────────────────────────────────────────────────

    public int getSeatsAvailable() {
        return totalSeats - seatsBooked;
    }

    public boolean isFull() {
        return seatsBooked >= totalSeats;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                             { return id; }
    public String getTitle()                        { return title; }
    public String getDescription()                  { return description; }
    public LocalDateTime getWorkshopDate()          { return workshopDate; }
    public String getLocation()                     { return location; }
    public String getDuration()                     { return duration; }
    public int getTotalSeats()                      { return totalSeats; }
    public int getSeatsBooked()                     { return seatsBooked; }
    public BigDecimal getPrice()                    { return price; }
    public String getCoverImage()                   { return coverImage; }
    public WorkshopStatus getStatus()               { return status; }
    public User getPhotographer()                   { return photographer; }
    public List<WorkshopParticipant> getParticipants() { return participants; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }

    public void setTitle(String title)                      { this.title = title; }
    public void setDescription(String description)          { this.description = description; }
    public void setWorkshopDate(LocalDateTime workshopDate) { this.workshopDate = workshopDate; }
    public void setLocation(String location)                { this.location = location; }
    public void setDuration(String duration)                { this.duration = duration; }
    public void setTotalSeats(int totalSeats)               { this.totalSeats = totalSeats; }
    public void setSeatsBooked(int seatsBooked)             { this.seatsBooked = seatsBooked; }
    public void setPrice(BigDecimal price)                  { this.price = price; }
    public void setCoverImage(String coverImage)            { this.coverImage = coverImage; }
    public void setStatus(WorkshopStatus status)            { this.status = status; }
    public void setPhotographer(User photographer)          { this.photographer = photographer; }
}