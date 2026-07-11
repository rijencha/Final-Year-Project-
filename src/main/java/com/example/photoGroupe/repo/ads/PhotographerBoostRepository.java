package com.example.photoGroupe.repo.ads;

import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.model.ads.PhotographerBoost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PhotographerBoostRepository extends JpaRepository<PhotographerBoost, Long> {
    Optional<PhotographerBoost> findByTransactionUuid(String transactionUuid);

    @Query("SELECT b.photographer.id FROM PhotographerBoost b WHERE b.status = 'ACTIVE' AND b.endAt > :now")
    Set<Long> findActivePhotographerIds(@Param("now") LocalDateTime now);

    @Query("SELECT b.photographer.id AS photographerId, b.endAt AS endAt " +
            "FROM PhotographerBoost b WHERE b.status = 'ACTIVE' AND b.endAt > :now")
    List<ActiveBoostProjection> findActiveBoosts(@Param("now") LocalDateTime now);

    interface ActiveBoostProjection {
        Long getPhotographerId();
        LocalDateTime getEndAt();
    }

    List<PhotographerBoost> findByStatusAndEndAtBefore(BannerStatus status, LocalDateTime now);

    Page<PhotographerBoost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT COALESCE(SUM(b.amountPaid), 0) FROM PhotographerBoost b WHERE b.status IN ('ACTIVE','EXPIRED')")
    BigDecimal sumRevenue();

    Optional<PhotographerBoost> findFirstByPhotographerIdAndStatusAndEndAtAfterOrderByEndAtDesc(
            Long photographerId, BannerStatus status, LocalDateTime now);

    List<PhotographerBoost> findByPhotographerIdOrderByCreatedAtDesc(Long photographerId);
}