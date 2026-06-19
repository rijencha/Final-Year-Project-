package com.example.photoGroupe.repo.payment;

import com.example.photoGroupe.model.booking.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionUuid(String transactionUuid);
    Optional<Payment> findByBookingIdAndStatus(Long bookingId, String status);
    Optional<Payment> findTopByBookingIdAndStatusOrderByCreatedAtDesc(Long bookingId, String status);
    List<Payment> findAllByBookingIdAndStatus(Long bookingId, String status);
}
