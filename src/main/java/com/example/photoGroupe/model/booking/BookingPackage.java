package com.example.photoGroupe.model.booking;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_packages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "package_type", nullable = false)
    private PackageType packageType;   // PREMIUM, CUSTOM

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private PhotographerPackage template;

    @Column(name = "counter_price")
    private BigDecimal counterPrice;  // client's counter-offer

    @Column(name = "negotiation_round", nullable = false)
    private int negotiationRound = 1;  // increments each counter

    @Column(name = "last_action_by")
    private String lastActionBy;

    @Column(length = 1000)
    private String description;        // what's included

    @Column(name = "delivery_days")
    private Integer deliveryDays;      // e.g. edited photos delivered in X days

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageStatus status;      // SENT, ACCEPTED, REJECTED

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getCounterPrice() {
        return counterPrice;
    }

    public void setCounterPrice(BigDecimal counterPrice) {
        this.counterPrice = counterPrice;
    }

    public int getNegotiationRound() {
        return negotiationRound;
    }

    public void setNegotiationRound(int negotiationRound) {
        this.negotiationRound = negotiationRound;
    }

    public String getLastActionBy() {
        return lastActionBy;
    }

    public void setLastActionBy(String lastActionBy) {
        this.lastActionBy = lastActionBy;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PhotographerPackage getTemplate()        { return template; }

    public void setTemplate(PhotographerPackage t)  { this.template = t; }
}
