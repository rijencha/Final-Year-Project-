package com.example.photoGroupe.service.testing;

import com.example.photoGroupe.dto.payout.BalanceResponse;
import com.example.photoGroupe.dto.payout.WithdrawalCreateRequest;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.payout.PayoutAccount;
import com.example.photoGroupe.model.payout.PayoutAccountType;
import com.example.photoGroupe.model.payout.WithdrawalRequest;
import com.example.photoGroupe.model.payout.WithdrawalStatus;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.repo.payout.PayoutAccountRepository;
import com.example.photoGroupe.repo.payout.WithdrawalRequestRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.payout.WithdrawalServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock private PayoutAccountRepository accountRepository;
    @Mock private WithdrawalRequestRepository withdrawalRepository;
    @Mock private PayoutRepository payoutRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private WithdrawalServiceImpl withdrawalService;

    private User photographer;
    private PayoutAccount account;

    @BeforeEach
    void setUp() {
        photographer = new User();
        photographer.setId(1L);
        photographer.setRole(Role.PHOTOGRAPHER);

        account = PayoutAccount.builder()
                .id(1L)
                .photographer(photographer)
                .type(PayoutAccountType.ESEWA)
                .esewaId("9800000000")
                .build();
    }

    // ── getBalance() ──────────────────────────────────────────

    @Test
    void getBalance_subtractsReservedFromReleased() {
        when(payoutRepository.sumReleasedAmount(1L)).thenReturn(new BigDecimal("5000"));
        when(withdrawalRepository.sumReservedAmount(1L)).thenReturn(new BigDecimal("2000"));

        BalanceResponse balance = withdrawalService.getBalance(photographer);

        assertEquals(new BigDecimal("3000"), balance.availableBalance());
    }

    @Test
    void getBalance_neverGoesNegative() {
        when(payoutRepository.sumReleasedAmount(1L)).thenReturn(new BigDecimal("1000"));
        when(withdrawalRepository.sumReservedAmount(1L)).thenReturn(new BigDecimal("5000"));

        BalanceResponse balance = withdrawalService.getBalance(photographer);

        assertEquals(0, balance.availableBalance().compareTo(BigDecimal.ZERO));
    }

    // ── requestWithdrawal() ───────────────────────────────────

    @Test
    void requestWithdrawal_throwsWhenBelowMinimum() {
        WithdrawalCreateRequest req = new WithdrawalCreateRequest(new BigDecimal("500"), 1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> withdrawalService.requestWithdrawal(photographer, req));
        assertTrue(ex.getMessage().contains("Minimum withdrawal"));
    }

    @Test
    void requestWithdrawal_throwsWhenNotPhotographer() {
        photographer.setRole(Role.USER);
        WithdrawalCreateRequest req = new WithdrawalCreateRequest(new BigDecimal("2000"),1L);

        assertThrows(RuntimeException.class,
                () -> withdrawalService.requestWithdrawal(photographer, req));
    }

    @Test
    void requestWithdrawal_throwsWhenAccountBelongsToSomeoneElse() {
        User otherPhotographer = new User();
        otherPhotographer.setId(2L);
        account.setPhotographer(otherPhotographer);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        WithdrawalCreateRequest req = new WithdrawalCreateRequest( new BigDecimal("2000"),1L);

        assertThrows(RuntimeException.class,
                () -> withdrawalService.requestWithdrawal(photographer, req));
    }

    @Test
    void requestWithdrawal_throwsWhenAmountExceedsBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(payoutRepository.sumReleasedAmount(1L)).thenReturn(new BigDecimal("3000"));
        when(withdrawalRepository.sumReservedAmount(1L)).thenReturn(BigDecimal.ZERO);

        WithdrawalCreateRequest req = new WithdrawalCreateRequest(new BigDecimal("5000"),1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> withdrawalService.requestWithdrawal(photographer, req));
        assertEquals("Amount exceeds available balance", ex.getMessage());
    }

    @Test
    void requestWithdrawal_succeedsAndSetsStatusPending() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(payoutRepository.sumReleasedAmount(1L)).thenReturn(new BigDecimal("5000"));
        when(withdrawalRepository.sumReservedAmount(1L)).thenReturn(BigDecimal.ZERO);
        when(withdrawalRepository.save(any(WithdrawalRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        WithdrawalCreateRequest req = new WithdrawalCreateRequest( new BigDecimal("2000"),1L);
        var response = withdrawalService.requestWithdrawal(photographer, req);

        assertEquals(WithdrawalStatus.PENDING, response.status());
        verify(notificationService).create(eq(photographer), eq(photographer),
                eq("WITHDRAWAL_REQUESTED"), anyString(), anyString());
    }

    // ── markFailed() ──────────────────────────────────────────

    @Test
    void markFailed_throwsIfAlreadyCompleted() {
        WithdrawalRequest w = WithdrawalRequest.builder()
                .id(1L).photographer(photographer).amount(new BigDecimal("2000"))
                .payoutAccount(account).status(WithdrawalStatus.COMPLETED).build();
        when(withdrawalRepository.findById(1L)).thenReturn(Optional.of(w));

        assertThrows(RuntimeException.class, () -> withdrawalService.markFailed(1L, "bank rejected"));
    }

    // ── markCompleted() ───────────────────────────────────────

    @Test
    void markCompleted_isIdempotent() {
        WithdrawalRequest w = WithdrawalRequest.builder()
                .id(1L).photographer(photographer).amount(new BigDecimal("2000"))
                .payoutAccount(account).status(WithdrawalStatus.COMPLETED).build();
        when(withdrawalRepository.findById(1L)).thenReturn(Optional.of(w));

        withdrawalService.markCompleted(1L, "ref-123");

        verify(withdrawalRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), anyString(), anyString(), anyString());
    }
}