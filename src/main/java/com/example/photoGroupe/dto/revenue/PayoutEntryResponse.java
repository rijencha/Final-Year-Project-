package com.example.photoGroupe.dto.revenue;

import com.example.photoGroupe.model.booking.Payout;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PayoutEntryResponse {
    private Long id;
    private String sourceType;
    private String photographerName;
    private BigDecimal totalAmount;
    private BigDecimal adminCommission;
    private BigDecimal photographerAmount;
    private String status;
    private LocalDateTime releasedAt;
    private LocalDateTime createdAt;

    public PayoutEntryResponse(Payout p) {
        this.id = p.getId();
        this.sourceType = p.getSourceType();
        this.photographerName = p.getPhotographer() != null ? p.getPhotographer().getFullName() : null;
        this.totalAmount = p.getTotalAmount();
        this.adminCommission = p.getCommissionAmount();
        this.photographerAmount = p.getPhotographerAmount();
        this.status = p.getStatus();
        this.releasedAt = p.getReleasedAt();
        this.createdAt = p.getCreatedAt();
    }
}
