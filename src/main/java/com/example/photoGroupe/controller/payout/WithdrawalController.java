package com.example.photoGroupe.controller.payout;

import com.example.photoGroupe.dto.payout.*;
import com.example.photoGroupe.dto.photographer.PhotographerRevenueResponse;
import com.example.photoGroupe.model.payout.WithdrawalStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.payment.RevenueService;
import com.example.photoGroupe.service.payout.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users/payouts")
@RequiredArgsConstructor
public class WithdrawalController {

    private final WithdrawalService withdrawalService;
    private final RevenueService  revenueService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(withdrawalService.getBalance(currentUser.getUser()));
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<PayoutAccountResponse>> getAccounts(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(withdrawalService.getAccounts(currentUser.getUser()));
    }

    @PostMapping("/accounts")
    public ResponseEntity<PayoutAccountResponse> addAccount(@RequestBody PayoutAccountRequest req,
                                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(withdrawalService.addAccount(currentUser.getUser(), req));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id,
                                              @AuthenticationPrincipal CustomUserDetails currentUser) {
        withdrawalService.deleteAccount(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<WithdrawalResponse> requestWithdrawal(@RequestBody WithdrawalCreateRequest req,
                                                                @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(withdrawalService.requestWithdrawal(currentUser.getUser(), req));
    }

    @GetMapping
    public ResponseEntity<Page<WithdrawalResponse>> myWithdrawals(@AuthenticationPrincipal CustomUserDetails currentUser,
                                                                  Pageable pageable) {
        return ResponseEntity.ok(withdrawalService.getMyWithdrawals(currentUser.getUser(), pageable));
    }

    @GetMapping("/revenue")
    public ResponseEntity<PhotographerRevenueResponse> getRevenue(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(revenueService.getRevenue(currentUser.getUser()));
    }

    // ── Admin ────────────────────────────────────────────────────────────

    @GetMapping("/admin/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<WithdrawalResponse>> getPending(Pageable pageable) {
        return ResponseEntity.ok(withdrawalService.getPending(pageable));
    }

    @PatchMapping("/admin/{id}/processing")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WithdrawalResponse> markProcessing(@PathVariable Long id) {
        return ResponseEntity.ok(withdrawalService.markProcessing(id));
    }

    @PatchMapping("/admin/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WithdrawalResponse> markCompleted(@PathVariable Long id, @RequestParam String reference) {
        return ResponseEntity.ok(withdrawalService.markCompleted(id, reference));
    }

    @PatchMapping("/admin/{id}/fail")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<WithdrawalResponse> markFailed(@PathVariable Long id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(withdrawalService.markFailed(id, reason));
    }

    // controller/payout/WithdrawalController.java — add alongside /admin/pending
    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<WithdrawalResponse>> getAll(
            @RequestParam(required = false) WithdrawalStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(withdrawalService.getAll(status, pageable));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<WithdrawalReceiptResponse> getReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(withdrawalService.getReceipt(id, currentUser.getUser()));
    }

    @GetMapping("/{id}/receipt/download")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {
        byte[] pdf = withdrawalService.generateReceiptPdf(id, currentUser.getUser());
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"withdrawal-receipt-" + id + ".pdf\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}