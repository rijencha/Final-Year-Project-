package com.example.photoGroupe.repo.payment;

import com.example.photoGroupe.model.booking.Payout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {
    Optional<Payout> findByBookingId(Long bookingId);
}