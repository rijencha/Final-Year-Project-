package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.model.event.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {
    Page<EventRequest> findByStatus(EventRequestStatus status, Pageable pageable);
    Page<EventRequest> findByClientId(Long clientId, Pageable pageable);
    Page<EventRequest> findByEventTypeAndStatus(EventType type, EventRequestStatus status, Pageable pageable);

    @Query("""
    SELECT e FROM EventRequest e
    WHERE e.status = :status
    AND (
        e.eventType = :type
        OR LOWER(e.customEventType) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    Page<EventRequest> findByTypeOrCustomType(
            @Param("type") EventType type,
            @Param("keyword") String keyword,
            @Param("status") EventRequestStatus status,
            Pageable pageable);

    // For custom-only filter (when user picks a custom type like "portrait session")
    Page<EventRequest> findByCustomEventTypeIgnoreCaseAndStatus(
            String customEventType, EventRequestStatus status, Pageable pageable);

    List<EventRequest> findByStatusAndDeadlineAtBefore(EventRequestStatus status, LocalDateTime dateTime);
}
