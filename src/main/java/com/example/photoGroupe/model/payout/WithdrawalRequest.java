package com.example.photoGroupe.model.payout;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_requests")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WithdrawalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_account_id", nullable = false)
    private PayoutAccount payoutAccount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;

    private String reference;       // set by admin once the bank/eSewa transfer is done
    private String failureReason;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() { this.requestedAt = LocalDateTime.now(); }
}