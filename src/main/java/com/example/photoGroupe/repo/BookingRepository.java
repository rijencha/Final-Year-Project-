package com.example.photoGroupe.repo;


import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Client
    Page<Booking> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);
    Page<Booking> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, BookingStatus status, Pageable pageable);

    // Photographer
    Page<Booking> findByPhotographerIdOrderByCreatedAtDesc(Long photographerId, Pageable pageable);
    Page<Booking> findByPhotographerIdAndStatusOrderByCreatedAtDesc(Long photographerId, BookingStatus status, Pageable pageable);

    // Conflict check — is photographer already booked for that date?
    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.photographer.id = :photographerId
        AND b.status IN ('PENDING', 'CONFIRMED')
        AND FUNCTION('DATE', b.eventDate) = FUNCTION('DATE', :eventDate)
    """)
    boolean isPhotographerBooked(
            @Param("photographerId") Long photographerId,
            @Param("eventDate") LocalDateTime eventDate
    );

    // Admin
    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status, Pageable pageable);

    // Earnings
    @Query("SELECT COALESCE(SUM(b.price), 0) FROM Booking b WHERE b.photographer.id = :id AND b.status = 'COMPLETED'")
    BigDecimal getTotalEarnings(@Param("id") Long photographerId);

    // Counts
    long countByPhotographerIdAndStatus(Long photographerId, BookingStatus status);
    long countByClientIdAndStatus(Long clientId, BookingStatus status);
    long countByPhotographerId(Long photographerId);
    Optional<Booking> findByTransactionId(String transactionId);

    /**
     * All bookings (optionally filtered by status) for event-type grouping.
     * Grouping itself happens in the service layer since eventType (enum)
     * and customEventType (String) can't be safely COALESCE'd in JPQL.
     */
    @Query("""
        SELECT b FROM Booking b
        WHERE (:status IS NULL OR b.status = :status)
        ORDER BY b.eventType ASC NULLS LAST, b.createdAt DESC
        """)
    List<Booking> findAllForEventTypeGrouping(@Param("status") BookingStatus status);

    @Query("""
        SELECT b
        FROM Booking b
        LEFT JOIN PhotographerSpecialization ps ON ps.photographer.id = b.photographer.id
        WHERE ((:specName = 'Uncategorized' AND ps.id IS NULL)
           OR LOWER(COALESCE(ps.category.name, ps.customType)) = LOWER(:specName))
          AND (:status IS NULL OR b.status = :status)
        ORDER BY b.createdAt DESC
        """)
    List<Booking> findBookingsBySpecializationName(
            @Param("specName") String specName,
            @Param("status") BookingStatus status);
}
