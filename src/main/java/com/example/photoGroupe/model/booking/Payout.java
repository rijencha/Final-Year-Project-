package com.example.photoGroupe.model.booking;

import com.example.photoGroupe.model.User;
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
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;       // full booking price

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionAmount;  // 12%

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal photographerAmount; // 88%

    @Column(nullable = false)
    private String status; // PENDING, RELEASED

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
}