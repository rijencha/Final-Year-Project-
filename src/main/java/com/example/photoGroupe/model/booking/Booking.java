package com.example.photoGroupe.model.booking;

import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventType;
import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    // Optional — if booked from a bid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    private Bid bid;

    @Column(nullable = false)
    private String eventTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String location;

    private String description;

    private Integer durationHours;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private String rejectionReason;     // photographer fills this on rejection
    private String cancellationReason;  // client fills this on cancellation
    private String specialRequests;     // client notes

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt     = LocalDateTime.now();
        this.updatedAt     = LocalDateTime.now();
        this.status        = BookingStatus.PENDING;
        this.paymentStatus = PaymentStatus.UNPAID;
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // ── Getters ──────────────────────────────────────────────────────────
    public Long getId()                       { return id; }
    public User getClient()                   { return client; }
    public User getPhotographer()             { return photographer; }
    public Bid getBid()                       { return bid; }
    public String getEventTitle()             { return eventTitle; }
    public EventType getEventType()           { return eventType; }
    public LocalDateTime getEventDate()       { return eventDate; }
    public String getLocation()               { return location; }
    public String getDescription()            { return description; }
    public Integer getDurationHours()         { return durationHours; }
    public BigDecimal getPrice()              { return price; }
    public BookingStatus getStatus()          { return status; }
    public PaymentStatus getPaymentStatus()   { return paymentStatus; }
    public String getRejectionReason()        { return rejectionReason; }
    public String getCancellationReason()     { return cancellationReason; }
    public String getSpecialRequests()        { return specialRequests; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setStatus(BookingStatus s)            { this.status = s; }
    public void setPaymentStatus(PaymentStatus p)     { this.paymentStatus = p; }
    public void setRejectionReason(String r)          { this.rejectionReason = r; }
    public void setCancellationReason(String r)       { this.cancellationReason = r; }
    public void setBid(Bid bid)                       { this.bid = bid; }
}
