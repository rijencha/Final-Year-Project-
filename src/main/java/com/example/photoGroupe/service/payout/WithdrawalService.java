package com.example.photoGroupe.service.payout;

import com.example.photoGroupe.dto.payout.*;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.payout.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public interface WithdrawalService {
    BalanceResponse getBalance(User photographer);
    List<PayoutAccountResponse> getAccounts(User photographer);
    PayoutAccountResponse addAccount(User photographer, PayoutAccountRequest req);
    void deleteAccount(Long accountId, User photographer);
    WithdrawalResponse requestWithdrawal(User photographer, WithdrawalCreateRequest req);
    Page<WithdrawalResponse> getMyWithdrawals(User photographer, Pageable pageable);

    // Admin
    Page<WithdrawalResponse> getPending(Pageable pageable);
    WithdrawalResponse markProcessing(Long withdrawalId);
    WithdrawalResponse markCompleted(Long withdrawalId, String reference);
    WithdrawalResponse markFailed(Long withdrawalId, String reason);
    // service/payout/WithdrawalService.java — add to the interface
    Page<WithdrawalResponse> getAll(WithdrawalStatus status, Pageable pageable);
    WithdrawalReceiptResponse getReceipt(Long withdrawalId, User photographer);
    byte[] generateReceiptPdf(Long withdrawalId, User photographer) throws IOException;
}