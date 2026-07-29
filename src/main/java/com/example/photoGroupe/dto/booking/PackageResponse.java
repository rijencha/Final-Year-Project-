package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.PackageStatus;
import com.example.photoGroupe.model.booking.PackageType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public class PackageResponse {
    private Long id;
    private Long bookingId;
    private BigDecimal price;
    private String name;
    private PackageType packageType;
    private String description;
    private Integer deliveryDays;
    private PackageStatus status;
    private int negotiationRound;
    private String lastActionBy;
    private BigDecimal counterPrice;  // so client sees what they offered
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
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

    public BigDecimal getCounterPrice() {
        return counterPrice;
    }

    public void setCounterPrice(BigDecimal counterPrice) {
        this.counterPrice = counterPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
