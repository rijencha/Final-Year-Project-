package com.example.photoGroupe.model.booking;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "transaction_uuid", unique = true, nullable = false)
    private String transactionUuid;

    @Column(name = "product_code", nullable = false)
    private String productCode;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
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

    // Getters
    public Long getId()                  { return id; }
    public String getTransactionUuid()   { return transactionUuid; }
    public String getProductCode()       { return productCode; }
    public Double getAmount()            { return amount; }
    public String getStatus()            { return status; }
    public User getUser()                { return user; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
    public Long getBookingId()              { return bookingId; }

    // Setters
    public void setId(Long id)                          { this.id = id; }
    public void setTransactionUuid(String uuid)         { this.transactionUuid = uuid; }
    public void setProductCode(String productCode)      { this.productCode = productCode; }
    public void setAmount(Double amount)                { this.amount = amount; }
    public void setStatus(String status)                { this.status = status; }
    public void setUser(User user)                      { this.user = user; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }
    public void setBookingId(Long id)       { this.bookingId = id; }
}
