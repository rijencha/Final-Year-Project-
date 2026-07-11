package com.example.photoGroupe.dto.revenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueSummaryResponse {

    private BigDecimal totalRevenue;          // sum of all totalAmount
    private BigDecimal totalAdminCommission;  // sum of all commissionAmount
    private BigDecimal totalPhotographerPayout; // sum of all photographerAmount

    private LocalDateTime periodStart;   // earliest transaction date included in this summary
    private LocalDateTime periodEnd;     // latest transaction date included in this summary
    private LocalDateTime generatedAt;   // when this summary was computed

    private List<SourceBreakdown> breakdown;  // per BOOKING / WORKSHOP / BANNER / BOOST

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceBreakdown {
        private String sourceType;
        private long count;
        private BigDecimal totalRevenue;
        private BigDecimal adminCommission;
        private BigDecimal photographerAmount;
        private LocalDateTime periodStart;   // ← add
        private LocalDateTime periodEnd;
    }

}