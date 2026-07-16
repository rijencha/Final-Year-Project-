package com.example.photoGroupe.dto.payout;

import com.example.photoGroupe.model.payout.WithdrawalRequest;
import com.example.photoGroupe.model.payout.WithdrawalStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalResponse(
        Long id,
        BigDecimal amount,
        WithdrawalStatus status,
        PayoutAccountResponse account,
        String reference,
        String failureReason,
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        Long photographerId,
        String photographerName,
        String photographerUsername,
        String photographerEmail,
        String photographerProfilePicture
) {
    public static WithdrawalResponse from(WithdrawalRequest w) {
        var photographer = w.getPhotographer();
        return new WithdrawalResponse(
                w.getId(), w.getAmount(), w.getStatus(),
                PayoutAccountResponse.from(w.getPayoutAccount()),
                w.getReference(), w.getFailureReason(),
                w.getRequestedAt(), w.getProcessedAt(),
                photographer.getId(),
                photographer.getFullName(),
                photographer.getActualUsername(),
                photographer.getEmail(),
                photographer.getProfilePicture()
        );
    }
}