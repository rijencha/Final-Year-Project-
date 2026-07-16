package com.example.photoGroupe.dto.payout;

import com.example.photoGroupe.model.payout.PayoutAccount;
import com.example.photoGroupe.model.payout.PayoutAccountType;

public record PayoutAccountResponse(
        Long id,
        PayoutAccountType type,
        String label,
        String maskedDetail,
        String holderName,
        boolean isDefault
) {
    public static PayoutAccountResponse from(PayoutAccount acc) {
        String label = acc.getType() == PayoutAccountType.BANK ? acc.getBankName() : "eSewa";
        String masked = acc.getType() == PayoutAccountType.BANK
                ? "•••• " + last4(acc.getAccountNumber())
                : maskEsewa(acc.getEsewaId());
        return new PayoutAccountResponse(acc.getId(), acc.getType(), label, masked, acc.getHolderName(), acc.isDefault());
    }

    private static String last4(String s) {
        return (s == null || s.length() < 4) ? s : s.substring(s.length() - 4);
    }

    private static String maskEsewa(String id) {
        return (id == null || id.length() <= 4) ? id : id.substring(0, 2) + "••••••" + id.substring(id.length() - 2);
    }
}