package com.example.photoGroupe.service.payment;

import com.example.photoGroupe.dto.revenue.PayoutEntryResponse;
import com.example.photoGroupe.dto.revenue.RevenueSummaryResponse;

import java.math.BigDecimal;
import java.util.List;

public interface RevenueService {

    RevenueSummaryResponse getRevenueSummary();

    List<PayoutEntryResponse> getAllPayouts();

    List<PayoutEntryResponse> getPayoutsBySource(String sourceType);

    BigDecimal getPhotographerEarnings(Long photographerId);
}