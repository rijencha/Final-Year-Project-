package com.example.photoGroupe.dto.ads;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdatePricingRequest {
    private BigDecimal bannerRatePerDay;
    private BigDecimal boostRatePerDay;
}