package com.example.photoGroupe.dto.payout;

import java.math.BigDecimal;

public record WithdrawalCreateRequest(BigDecimal amount, Long payoutAccountId) {}