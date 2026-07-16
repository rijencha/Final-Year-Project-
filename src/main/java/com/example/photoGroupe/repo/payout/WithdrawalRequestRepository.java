package com.example.photoGroupe.repo.payout;

import com.example.photoGroupe.model.payout.WithdrawalRequest;
import com.example.photoGroupe.model.payout.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    Page<WithdrawalRequest> findByPhotographerIdOrderByRequestedAtDesc(Long photographerId, Pageable pageable);

    Page<WithdrawalRequest> findByStatusOrderByRequestedAtAsc(WithdrawalStatus status, Pageable pageable);

    @Query("""
           SELECT COALESCE(SUM(w.amount), 0) FROM WithdrawalRequest w
           WHERE w.photographer.id = :photographerId
             AND w.status IN ('PENDING', 'PROCESSING', 'COMPLETED')
           """)
    BigDecimal sumReservedAmount(@Param("photographerId") Long photographerId);

    List<WithdrawalRequest> findByPayoutAccountId(Long payoutAccountId);

    // repo/payout/WithdrawalRequestRepository.java
    Page<WithdrawalRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);

    Page<WithdrawalRequest> findByStatusOrderByRequestedAtDesc(WithdrawalStatus status, Pageable pageable);
}