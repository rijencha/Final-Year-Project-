package com.example.photoGroupe.model.ads;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_pricing")
@Getter
@Setter
public class AdPricing {
    @Id
    private Long id = 1L; // singleton row

    private BigDecimal bannerRatePerDay;
    private BigDecimal boostRatePerDay;

    private LocalDateTime updatedAt;
}