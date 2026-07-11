package com.example.photoGroupe.model.booking;

import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.workshop.Workshop;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payouts")
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    @Column(name = "source_type", nullable = false)
    private String sourceType; // "BOOKING", "WORKSHOP", "BANNER", "BOOST"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = true)   // ✅ now nullable — banners have no photographer
    private User photographer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal photographerAmount;

    @Column(nullable = false)
    private String status;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId()                              { return id; }
    public Booking getBooking()                      { return booking; }
    public User getPhotographer()                    { return photographer; }
    public BigDecimal getTotalAmount()               { return totalAmount; }
    public BigDecimal getCommissionAmount()          { return commissionAmount; }
    public BigDecimal getPhotographerAmount()        { return photographerAmount; }
    public String getStatus()                        { return status; }
    public LocalDateTime getReleasedAt()             { return releasedAt; }
    public LocalDateTime getCreatedAt()              { return createdAt; }

    public void setBooking(Booking b)                { this.booking = b; }
    public void setPhotographer(User u)              { this.photographer = u; }
    public void setTotalAmount(BigDecimal a)         { this.totalAmount = a; }
    public void setCommissionAmount(BigDecimal a)    { this.commissionAmount = a; }
    public void setPhotographerAmount(BigDecimal a)  { this.photographerAmount = a; }
    public void setStatus(String s)                  { this.status = s; }
    public void setReleasedAt(LocalDateTime t)       { this.releasedAt = t; }

    public Workshop getWorkshop() { return workshop; }
    public void setWorkshop(Workshop workshop) { this.workshop = workshop; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
}