package com.example.photoGroupe.service.ads;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AdPricingConfig {
    private static final BigDecimal BANNER_RATE_PER_DAY = new BigDecimal("300.00");
    private static final BigDecimal BOOST_RATE_PER_DAY   = new BigDecimal("200.00");

    public BigDecimal bannerPrice(int days) {
        return BANNER_RATE_PER_DAY.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal boostPrice(int days) {
        return BOOST_RATE_PER_DAY.multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }
}
