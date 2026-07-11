package com.example.photoGroupe.service.payment;

import com.example.photoGroupe.dto.revenue.PayoutEntryResponse;
import com.example.photoGroupe.dto.revenue.RevenueSummaryResponse;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final PayoutRepository payoutRepository;

    @Override
    public RevenueSummaryResponse getRevenueSummary() {
        List<Object[]> rows = payoutRepository.aggregateBySourceType();

        List<RevenueSummaryResponse.SourceBreakdown> breakdown = rows.stream()
                .map(r -> RevenueSummaryResponse.SourceBreakdown.builder()
                        .sourceType((String) r[0])
                        .count((Long) r[1])
                        .totalRevenue((BigDecimal) r[2])
                        .adminCommission((BigDecimal) r[3])
                        .photographerAmount((BigDecimal) r[4])
                        .periodStart((LocalDateTime) r[5])   // ← add
                        .periodEnd((LocalDateTime) r[6])     // ← add
                        .build())
                .toList();

        return RevenueSummaryResponse.builder()
                .totalRevenue(payoutRepository.sumTotalRevenue())
                .totalAdminCommission(payoutRepository.sumAdminCommission())
                .totalPhotographerPayout(payoutRepository.sumPhotographerPayout())
                .periodStart(payoutRepository.findEarliestCreatedAt())
                .periodEnd(payoutRepository.findLatestCreatedAt())
                .generatedAt(LocalDateTime.now())
                .breakdown(breakdown)
                .build();
    }

    @Override
    public List<PayoutEntryResponse> getAllPayouts() {
        return payoutRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(PayoutEntryResponse::new)
                .toList();
    }

    @Override
    public List<PayoutEntryResponse> getPayoutsBySource(String sourceType) {
        return payoutRepository.findAllBySourceTypeOrderByCreatedAtDesc(sourceType.toUpperCase())
                .stream()
                .map(PayoutEntryResponse::new)
                .toList();
    }

    @Override
    public BigDecimal getPhotographerEarnings(Long photographerId) {
        return payoutRepository.sumPhotographerEarningsFromBookingAndWorkshop(photographerId);
    }
}
