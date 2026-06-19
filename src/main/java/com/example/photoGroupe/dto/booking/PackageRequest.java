package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.PackageType;

import java.math.BigDecimal;

public class PackageRequest {
    private Long bookingId;
    private BigDecimal price;
    private PackageType packageType;
    private String description;
    private Integer deliveryDays;
    private Long templateId;

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeliveryDays(Integer deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public String getDescription() {
        return description;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }
}
