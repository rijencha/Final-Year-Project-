package com.example.photoGroupe.repo.payment;

import com.example.photoGroupe.model.booking.BookingPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingPackageRepository extends JpaRepository<BookingPackage, Long> {
    Optional<BookingPackage> findByBookingId(Long bookingId);
    List<BookingPackage> findAllByBookingPhotographerId(Long photographerId);
    List<BookingPackage> findAllByBookingId(Long bookingId);

}
