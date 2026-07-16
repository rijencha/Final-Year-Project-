package com.example.photoGroupe.dto.payout;

import com.example.photoGroupe.model.payout.PayoutAccountType;

public record PayoutAccountRequest(
        PayoutAccountType type,
        String holderName,
        String bankName,
        String accountNumber,
        String esewaId
) {}