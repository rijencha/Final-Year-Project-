package com.example.photoGroupe.model.payout;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payout_accounts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PayoutAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutAccountType type;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    // BANK-only fields
    private String bankName;
    private String accountNumber;

    // ESEWA-only field
    private String esewaId;

    @Column(name = "is_default")
    private boolean isDefault;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }
}