package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.bidding.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByEventRequestId(Long eventRequestId);
    List<Bid> findByPhotographerId(Long photographerId);
    Optional<Bid> findByEventRequestIdAndPhotographerId(Long eventRequestId, Long photographerId);
    boolean existsByEventRequestIdAndPhotographerId(Long eventRequestId, Long photographerId);

    @Query("SELECT COALESCE(SUM(b.price), 0) FROM Bid b WHERE b.photographer.id = :photographerId AND b.status = 'ACCEPTED'")
    BigDecimal sumEarningsByPhotographerId(@Param("photographerId") Long photographerId);
}