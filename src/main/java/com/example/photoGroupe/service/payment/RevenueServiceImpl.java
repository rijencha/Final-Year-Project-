package com.example.photoGroupe.service.payment;

import com.example.photoGroupe.dto.photographer.PhotographerRevenueResponse;
import com.example.photoGroupe.dto.revenue.PayoutEntryResponse;
import com.example.photoGroupe.dto.revenue.RevenueSummaryResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.repo.payout.WithdrawalRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RevenueServiceImpl implements RevenueService {

    private final PayoutRepository payoutRepository;
    private final WithdrawalRequestRepository withdrawalRepository;

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

    public PhotographerRevenueResponse getRevenue(User photographer) {
        Long id = photographer.getId();

        BigDecimal received = nz(payoutRepository.sumReleasedAmount(id));
        BigDecimal pending  = nz(payoutRepository.sumPendingAmount(id));
        BigDecimal total    = received.add(pending);

        BigDecimal withdrawn = nz(withdrawalRepository.sumCompletedAmount(id));
        BigDecimal reserved  = nz(withdrawalRepository.sumReservedAmount(id)); // pending/processing withdrawals
        BigDecimal available = received.subtract(withdrawn).subtract(reserved).max(BigDecimal.ZERO);

        BigDecimal bookingRevenue  = nz(payoutRepository.sumAmountBySourceType(id, "BOOKING"));
        BigDecimal workshopRevenue = nz(payoutRepository.sumAmountBySourceType(id, "WORKSHOP"));

        return new PhotographerRevenueResponse(
                received, pending, total, withdrawn, available, bookingRevenue, workshopRevenue
        );
    }

    private BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
