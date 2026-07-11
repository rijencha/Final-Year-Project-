package com.example.photoGroupe.repo.payment;

import com.example.photoGroupe.model.booking.Payout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    Optional<Payout> findByBookingId(Long bookingId);

    List<Payout> findAllByOrderByCreatedAtDesc();

    List<Payout> findAllBySourceTypeOrderByCreatedAtDesc(String sourceType);

    @Query("SELECT COALESCE(SUM(p.totalAmount), 0) FROM Payout p")
    BigDecimal sumTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.commissionAmount), 0) FROM Payout p")
    BigDecimal sumAdminCommission();

    @Query("SELECT COALESCE(SUM(p.photographerAmount), 0) FROM Payout p")
    BigDecimal sumPhotographerPayout();

    @Query("SELECT p.sourceType, COUNT(p), SUM(p.totalAmount), SUM(p.commissionAmount), SUM(p.photographerAmount), " +
            "MIN(p.createdAt), MAX(p.createdAt) " +
            "FROM Payout p GROUP BY p.sourceType")
    List<Object[]> aggregateBySourceType();

    // photographer-specific totals (booking + workshop only, since banner/boost have zero photographer split)
    @Query("SELECT COALESCE(SUM(p.photographerAmount), 0) FROM Payout p " +
            "WHERE p.photographer.id = :photographerId AND p.sourceType IN ('BOOKING','WORKSHOP')")
    BigDecimal sumPhotographerEarningsFromBookingAndWorkshop(@Param("photographerId") Long photographerId);

    @Query("SELECT MIN(p.createdAt) FROM Payout p")
    LocalDateTime findEarliestCreatedAt();

    @Query("SELECT MAX(p.createdAt) FROM Payout p")
    LocalDateTime findLatestCreatedAt();

}