package com.example.photoGroupe.dto.photographer;

import java.math.BigDecimal;

public record PhotographerRevenueResponse(
        BigDecimal receivedRevenue,   // released to photographer (lifetime, before withdrawal)
        BigDecimal pendingRevenue,    // earned, awaiting release (e.g. workshop payouts not yet released)
        BigDecimal totalRevenue,      // receivedRevenue + pendingRevenue — everything ever earned
        BigDecimal withdrawnAmount,   // sum of COMPLETED withdrawals — already paid out to bank/eSewa
        BigDecimal availableBalance,  // receivedRevenue - withdrawnAmount - amount reserved in pending/processing withdrawals
        BigDecimal bookingRevenue,    // from BOOKING payouts (received + pending)
        BigDecimal workshopRevenue    // from WORKSHOP payouts (received + pending)
) {}