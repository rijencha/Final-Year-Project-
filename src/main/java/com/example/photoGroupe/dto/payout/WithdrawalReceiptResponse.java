package com.example.photoGroupe.dto.payout;

import com.example.photoGroupe.model.payout.PayoutAccountType;
import com.example.photoGroupe.model.payout.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WithdrawalReceiptResponse(
        Long withdrawalId,
        String photographerName,
        String photographerEmail,
        BigDecimal amount,
        WithdrawalStatus status,
        LocalDateTime requestedAt,
        LocalDateTime processedAt,
        String reference,
        String failureReason,
        PayoutAccountType accountType,
        String accountHolderName,
        String maskedAccountDetail   // masked bank acct no. or eSewa ID
) {
    public static WithdrawalReceiptResponse from(com.example.photoGroupe.model.payout.WithdrawalRequest w) {
        var account = w.getPayoutAccount();
        String masked = switch (account.getType()) {
            case BANK -> maskTail(account.getAccountNumber());
            case ESEWA -> maskTail(account.getEsewaId());
        };

        return new WithdrawalReceiptResponse(
                w.getId(),
                w.getPhotographer().getFullName(),
                w.getPhotographer().getEmail(),
                w.getAmount(),
                w.getStatus(),
                w.getRequestedAt(),
                w.getProcessedAt(),
                w.getReference(),
                w.getFailureReason(),
                account.getType(),
                account.getHolderName(),
                masked
        );
    }

    private static String maskTail(String value) {
        if (value == null || value.length() <= 4) return value;
        return "•".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}