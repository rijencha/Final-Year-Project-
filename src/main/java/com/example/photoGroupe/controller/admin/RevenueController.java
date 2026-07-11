package com.example.photoGroupe.controller.admin;

import com.example.photoGroupe.dto.revenue.PayoutEntryResponse;
import com.example.photoGroupe.dto.revenue.RevenueSummaryResponse;
import com.example.photoGroupe.service.payment.RevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RevenueController {

    private final RevenueService revenueService;

    @GetMapping("/summary")
    public RevenueSummaryResponse getSummary() {
        return revenueService.getRevenueSummary();
    }

    @GetMapping("/list")
    public List<PayoutEntryResponse> getAllPayouts() {
        return revenueService.getAllPayouts();
    }

    @GetMapping("/list/{sourceType}")
    public List<PayoutEntryResponse> getBySource(@PathVariable String sourceType) {
        return revenueService.getPayoutsBySource(sourceType);
    }

    @GetMapping("/photographer/{photographerId}/earnings")
    public BigDecimal getPhotographerEarnings(@PathVariable Long photographerId) {
        return revenueService.getPhotographerEarnings(photographerId);
    }
}
