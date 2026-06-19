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

    @Column(name = "booking_package_id")
    private Long bookingPackageId;
    // Optional — if booked from a bid
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id")
    private Bid bid;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(nullable = false)
    private String eventTitle;

    @Enumerated(EnumType.STRING)
    @Column
    private EventType eventType;

    @Column(name = "custom_event_type")
    private String customEventType;

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
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "escrow_status")
    private EscrowStatus escrowStatus;

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
    public String getTransactionId() { return transactionId; }
    public Long getBookingPackageId() { return bookingPackageId; }
    public String getCustomEventType() { return customEventType; }

    // ── Setters ──────────────────────────────────────────────────────────
    public void setBookingPackageId(Long bookingPackageId) { this.bookingPackageId = bookingPackageId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Payment getPayment()                   { return payment; }
    public EscrowStatus getEscrowStatus()         { return escrowStatus; }
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    public void setPayment(Payment p)             { this.payment = p; }
    public void setEscrowStatus(EscrowStatus e)   { this.escrowStatus = e; }
    // Add totalAmount helper (price is your total)
    public BigDecimal getTotalAmount() { return price; }
    public void setCustomEventType(String customEventType) { this.customEventType = customEventType; }
    public void setStatus(BookingStatus s)            { this.status = s; }
    public void setPaymentStatus(PaymentStatus p)     { this.paymentStatus = p; }
    public void setRejectionReason(String r)          { this.rejectionReason = r; }
    public void setCancellationReason(String r)       { this.cancellationReason = r; }
    public void setBid(Bid bid)                       { this.bid = bid; }
}
