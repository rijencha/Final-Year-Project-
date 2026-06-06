package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.model.event.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRequestRepository extends JpaRepository<EventRequest, Long> {
    Page<EventRequest> findByStatus(EventRequestStatus status, Pageable pageable);
    Page<EventRequest> findByClientId(Long clientId, Pageable pageable);
    Page<EventRequest> findByEventTypeAndStatus(EventType type, EventRequestStatus status, Pageable pageable);
}
