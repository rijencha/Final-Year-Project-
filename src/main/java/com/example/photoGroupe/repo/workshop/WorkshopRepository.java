package com.example.photoGroupe.repo.workshop;

import com.example.photoGroupe.model.workshop.Workshop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    /** All workshops by a specific photographer */
    List<Workshop> findByPhotographerIdOrderByWorkshopDateAsc(Long photographerId);

    /** Public listing — upcoming workshops with seats available */
    @Query("""
        SELECT w FROM Workshop w
        WHERE w.status = 'UPCOMING'
          AND w.seatsBooked < w.totalSeats
        ORDER BY w.workshopDate ASC
    """)
    Page<Workshop> findAvailable(Pageable pageable);

    /** Count confirmed participants */
    @Query("SELECT COUNT(p) FROM WorkshopParticipant p WHERE p.workshop.id = :workshopId AND p.status = 'CONFIRMED'")
    long countConfirmedParticipants(@Param("workshopId") Long workshopId);

    @Modifying
    @Query("""
        UPDATE Workshop w
        SET w.seatsBooked = w.seatsBooked + 1
        WHERE w.id = :workshopId
          AND w.seatsBooked < w.totalSeats
    """)
    int reserveSeat(@Param("workshopId") Long workshopId);

    /** Release a previously-reserved seat (cancelled / expired / failed payment). */
    @Modifying
    @Query("""
        UPDATE Workshop w
        SET w.seatsBooked = w.seatsBooked - 1
        WHERE w.id = :workshopId
          AND w.seatsBooked > 0
    """)
    int releaseSeat(@Param("workshopId") Long workshopId);

//    @Modifying
//    @Query("UPDATE Workshop w SET w.seatsBooked = w.seatsBooked + 1 WHERE w.id = :workshopId")
//    int confirmSeat(@Param("workshopId") Long workshopId);
}