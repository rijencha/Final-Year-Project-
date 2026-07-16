package com.example.photoGroupe.service.payout;

import com.example.photoGroupe.dto.payout.*;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.payout.*;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.repo.payout.PayoutAccountRepository;
import com.example.photoGroupe.repo.payout.WithdrawalRequestRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("1000");

    private final PayoutAccountRepository accountRepository;
    private final WithdrawalRequestRepository withdrawalRepository;
    private final PayoutRepository payoutRepository;
    private final NotificationService notificationService;

    // ── Balance ──────────────────────────────────────────────────────────

    @Override
    public BalanceResponse getBalance(User photographer) {
        BigDecimal released = payoutRepository.sumReleasedAmount(photographer.getId());
        BigDecimal reserved = withdrawalRepository.sumReservedAmount(photographer.getId());
        BigDecimal available = released.subtract(reserved).max(BigDecimal.ZERO);
        return new BalanceResponse(available, reserved, MIN_WITHDRAWAL);
    }

    // ── Payout accounts ──────────────────────────────────────────────────

    @Override
    public List<PayoutAccountResponse> getAccounts(User photographer) {
        return accountRepository.findByPhotographerIdOrderByCreatedAtAsc(photographer.getId())
                .stream().map(PayoutAccountResponse::from).toList();
    }

    @Override
    @Transactional
    public PayoutAccountResponse addAccount(User photographer, PayoutAccountRequest req) {
        if (req.holderName() == null || req.holderName().isBlank())
            throw new RuntimeException("Account holder name is required");

        if (req.type() == PayoutAccountType.BANK) {
            if (req.bankName() == null || req.bankName().isBlank())
                throw new RuntimeException("Bank name is required");
            if (req.accountNumber() == null || req.accountNumber().replaceAll("\\D", "").length() < 6)
                throw new RuntimeException("A valid account number is required");
        } else {
            if (req.esewaId() == null || req.esewaId().length() < 6)
                throw new RuntimeException("A valid eSewa ID is required");
        }

        boolean isFirst = accountRepository.findByPhotographerIdOrderByCreatedAtAsc(photographer.getId()).isEmpty();

        PayoutAccount acc = PayoutAccount.builder()
                .photographer(photographer)
                .type(req.type())
                .holderName(req.holderName())
                .bankName(req.type() == PayoutAccountType.BANK ? req.bankName() : null)
                .accountNumber(req.type() == PayoutAccountType.BANK ? req.accountNumber() : null)
                .esewaId(req.type() == PayoutAccountType.ESEWA ? req.esewaId() : null)
                .isDefault(isFirst)
                .build();

        return PayoutAccountResponse.from(accountRepository.save(acc));
    }

    @Override
    @Transactional
    public void deleteAccount(Long accountId, User photographer) {
        PayoutAccount acc = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Payout account not found"));

        if (!acc.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");

        boolean hasActiveWithdrawals = withdrawalRepository.findByPayoutAccountId(accountId)
                .stream()
                .anyMatch(w -> w.getStatus() == WithdrawalStatus.PENDING
                        || w.getStatus() == WithdrawalStatus.PROCESSING);

        if (hasActiveWithdrawals)
            throw new RuntimeException("Cannot remove an account with an in-progress withdrawal");

        accountRepository.delete(acc);
    }

    // ── Withdrawals ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public WithdrawalResponse requestWithdrawal(User photographer, WithdrawalCreateRequest req) {
        if (photographer.getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("Only photographers can withdraw earnings");

        if (req.amount() == null || req.amount().compareTo(MIN_WITHDRAWAL) < 0)
            throw new RuntimeException("Minimum withdrawal is NPR " + MIN_WITHDRAWAL.toPlainString());

        PayoutAccount account = accountRepository.findById(req.payoutAccountId())
                .orElseThrow(() -> new RuntimeException("Payout account not found"));

        if (!account.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized for this payout account");

        BigDecimal amount = req.amount().setScale(2, RoundingMode.HALF_UP);
        BalanceResponse balance = getBalance(photographer);

        if (amount.compareTo(balance.availableBalance()) > 0)
            throw new RuntimeException("Amount exceeds available balance");

        WithdrawalRequest withdrawal = WithdrawalRequest.builder()
                .photographer(photographer)
                .payoutAccount(account)
                .amount(amount)
                .status(WithdrawalStatus.PENDING)
                .build();

        withdrawalRepository.save(withdrawal);

        notificationService.create(
                photographer, photographer, "WITHDRAWAL_REQUESTED",
                "Your withdrawal request for NPR " + amount.toPlainString() + " has been submitted",
                "/dashboard/earnings"
        );

        return WithdrawalResponse.from(withdrawal);
    }

    @Override
    public Page<WithdrawalResponse> getMyWithdrawals(User photographer, Pageable pageable) {
        return withdrawalRepository
                .findByPhotographerIdOrderByRequestedAtDesc(photographer.getId(), pageable)
                .map(WithdrawalResponse::from);
    }

    // ── Admin ────────────────────────────────────────────────────────────

    @Override
    public Page<WithdrawalResponse> getPending(Pageable pageable) {
        return withdrawalRepository
                .findByStatusOrderByRequestedAtAsc(WithdrawalStatus.PENDING, pageable)
                .map(WithdrawalResponse::from);
    }

    @Override
    @Transactional
    public WithdrawalResponse markProcessing(Long withdrawalId) {
        WithdrawalRequest w = getOrThrow(withdrawalId);
        if (w.getStatus() != WithdrawalStatus.PENDING)
            throw new RuntimeException("Only pending withdrawals can move to processing");
        w.setStatus(WithdrawalStatus.PROCESSING);
        withdrawalRepository.save(w);
        return WithdrawalResponse.from(w);
    }

    @Override
    @Transactional
    public WithdrawalResponse markCompleted(Long withdrawalId, String reference) {
        WithdrawalRequest w = getOrThrow(withdrawalId);
        if (w.getStatus() == WithdrawalStatus.COMPLETED) return WithdrawalResponse.from(w); // idempotent

        w.setStatus(WithdrawalStatus.COMPLETED);
        w.setReference(reference);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w);

        notificationService.create(
                w.getPhotographer(), w.getPhotographer(), "WITHDRAWAL_COMPLETED",
                "NPR " + w.getAmount().toPlainString() + " has been sent to your " +
                        (w.getPayoutAccount().getType() == PayoutAccountType.BANK ? "bank account" : "eSewa account"),
                "/dashboard/earnings"
        );

        return WithdrawalResponse.from(w);
    }

    @Override
    @Transactional
    public WithdrawalResponse markFailed(Long withdrawalId, String reason) {
        WithdrawalRequest w = getOrThrow(withdrawalId);
        if (w.getStatus() == WithdrawalStatus.COMPLETED)
            throw new RuntimeException("Cannot fail a completed withdrawal");

        w.setStatus(WithdrawalStatus.FAILED);
        w.setFailureReason(reason);
        w.setProcessedAt(LocalDateTime.now());
        withdrawalRepository.save(w); // reserved sum drops automatically -> balance restored

        notificationService.create(
                w.getPhotographer(), w.getPhotographer(), "WITHDRAWAL_FAILED",
                "Your withdrawal request for NPR " + w.getAmount().toPlainString()
                        + " could not be completed" + (reason != null ? ": " + reason : "")
                        + ". The amount is back in your available balance.",
                "/dashboard/earnings"
        );

        return WithdrawalResponse.from(w);
    }

    // service/payout/WithdrawalServiceImpl.java — add the implementation
    @Override
    public Page<WithdrawalResponse> getAll(WithdrawalStatus status, Pageable pageable) {
        if (status != null) {
            return withdrawalRepository
                    .findByStatusOrderByRequestedAtDesc(status, pageable)
                    .map(WithdrawalResponse::from);
        }
        return withdrawalRepository
                .findAllByOrderByRequestedAtDesc(pageable)
                .map(WithdrawalResponse::from);
    }

    @Override
    public WithdrawalReceiptResponse getReceipt(Long withdrawalId, User photographer) {
        WithdrawalRequest w = getOwnedOrThrow(withdrawalId, photographer);
        return WithdrawalReceiptResponse.from(w);
    }

    @Override
    public byte[] generateReceiptPdf(Long withdrawalId, User photographer) throws IOException {
        WithdrawalRequest w = getOwnedOrThrow(withdrawalId, photographer);
        return buildReceiptPdf(WithdrawalReceiptResponse.from(w));
    }

    private WithdrawalRequest getOwnedOrThrow(Long id, User requester) {
        WithdrawalRequest w = getOrThrow(id);
        boolean isOwner = w.getPhotographer().getId().equals(requester.getId());
        boolean isAdmin = requester.getRole() == Role.ADMIN || requester.getRole() == Role.SUPER_ADMIN;
        if (!isOwner && !isAdmin)
            throw new AccessDeniedException("Not authorized to view this withdrawal");
        return w;
    }

    private byte[] buildReceiptPdf(WithdrawalReceiptResponse r) throws IOException {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            var page = new org.apache.pdfbox.pdmodel.PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
            doc.addPage(page);

            try (var content = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                drawWatermark(doc, content, page);

                float margin = 50;
                float y = page.getMediaBox().getHeight() - margin;
                var font = new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA_BOLD);
                var normal = new org.apache.pdfbox.pdmodel.font.PDType1Font(
                        org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA);

                content.beginText();
                content.setFont(font, 18);
                content.newLineAtOffset(margin, y);
                content.showText("Withdrawal Receipt");
                content.endText();
                y -= 40;

                String[][] rows = {
                        {"Withdrawal ID", "#" + r.withdrawalId()},
                        {"Status", r.status().name()},
                        {"Amount", "NPR " + r.amount().toPlainString()},
                        {"Requested At", String.valueOf(r.requestedAt())},
                        {"Processed At", r.processedAt() != null ? String.valueOf(r.processedAt()) : "—"},
                        {"Reference", r.reference() != null ? r.reference() : "—"},
                        {"Account Type", r.accountType().name()},
                        {"Account Holder", r.accountHolderName()},
                        {"Account Detail", r.maskedAccountDetail() != null ? r.maskedAccountDetail() : "—"},
                        {"Photographer", r.photographerName() + " (" + r.photographerEmail() + ")"},
                };
                if (r.failureReason() != null) {
                    // append as an extra row
                    String[][] withFailure = java.util.Arrays.copyOf(rows, rows.length + 1);
                    withFailure[rows.length] = new String[]{"Failure Reason", r.failureReason()};
                    rows = withFailure;
                }

                for (String[] row : rows) {
                    content.beginText();
                    content.setFont(font, 11);
                    content.newLineAtOffset(margin, y);
                    content.showText(row[0] + ":");
                    content.endText();

                    content.beginText();
                    content.setFont(normal, 11);
                    content.newLineAtOffset(margin + 160, y);
                    content.showText(row[1]);
                    content.endText();

                    y -= 22;
                }
            }

            var out = new java.io.ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }

    private void drawWatermark(
            org.apache.pdfbox.pdmodel.PDDocument doc,
            org.apache.pdfbox.pdmodel.PDPageContentStream content,
            org.apache.pdfbox.pdmodel.PDPage page
    ) throws IOException {
        try (var logoStream = getClass().getResourceAsStream("/branding/photogroupe logo.png")) {
            if (logoStream == null) return; // fail gracefully if the asset isn't bundled

            byte[] logoBytes = logoStream.readAllBytes();
            var logoImage = org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
                    .createFromByteArray(doc, logoBytes, "photogroupe-logo");

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            // Scale the watermark to ~60% of page width, preserving aspect ratio
            float targetWidth = pageWidth * 0.6f;
            float scale = targetWidth / logoImage.getWidth();
            float targetHeight = logoImage.getHeight() * scale;

            // Faint transparency via an extended graphics state
            var gs = new org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.08f);
            content.setGraphicsStateParameters(gs);

            content.saveGraphicsState();
            // Center + rotate ~20 degrees, like the on-screen version
            var matrix = new org.apache.pdfbox.util.Matrix();
            matrix.translate(pageWidth / 2f, pageHeight / 2f);
            matrix.rotate(Math.toRadians(20));
            matrix.translate(-targetWidth / 2f, -targetHeight / 2f);

            content.transform(matrix);
            content.drawImage(logoImage, 0, 0, targetWidth, targetHeight);
            content.restoreGraphicsState();

            // Reset alpha back to opaque for the rest of the page's real content
            var resetGs = new org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState();
            resetGs.setNonStrokingAlphaConstant(1.0f);
            content.setGraphicsStateParameters(resetGs);
        }
    }

    private WithdrawalRequest getOrThrow(Long id) {
        return withdrawalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Withdrawal request not found"));
    }
}