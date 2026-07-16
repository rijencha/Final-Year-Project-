package com.example.photoGroupe.dto.payout;

import java.math.BigDecimal;

public record BalanceResponse(BigDecimal availableBalance, BigDecimal pendingWithdrawals, BigDecimal minWithdrawal) {}