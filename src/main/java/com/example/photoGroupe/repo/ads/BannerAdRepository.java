package com.example.photoGroupe.repo.ads;

import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.BannerSlot;
import com.example.photoGroupe.model.ads.BannerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BannerAdRepository extends JpaRepository<BannerAd, Long> {
    Optional<BannerAd> findByTransactionUuid(String transactionUuid);

    @Query("SELECT b FROM BannerAd b WHERE b.status = 'ACTIVE' AND b.slot = :slot AND b.endAt > :now ORDER BY b.startAt ASC")
    List<BannerAd> findActiveBySlot(@Param("slot") BannerSlot slot, @Param("now") LocalDateTime now);

    List<BannerAd> findByStatusAndEndAtBefore(BannerStatus status, LocalDateTime now);

    // ── Admin ──
    Page<BannerAd> findByStatusOrderByCreatedAtAsc(BannerStatus status, Pageable pageable);
    Page<BannerAd> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.amountPaid), 0) FROM BannerAd b WHERE b.status IN ('ACTIVE','EXPIRED')")
    BigDecimal sumRevenue();
}
