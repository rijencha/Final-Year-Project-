package com.example.photoGroupe.service.event;

import com.example.photoGroupe.dto.eventandbid.EventRequestDTO;
import com.example.photoGroupe.dto.eventandbid.EventRequestResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.repo.EventRequestRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final NotificationService notificationService;

    @Override
    public EventRequestResponse create(User client, EventRequestDTO dto) {
        EventRequest event = EventRequest.builder()
                .client(client)
                .title(dto.getTitle())
                .eventType(dto.getEventType())
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .budgetMin(dto.getBudgetMin())
                .budgetMax(dto.getBudgetMax())
                .deadlineAt(dto.getDeadlineAt())
                .build();

        EventRequest saved = eventRequestRepository.save(event);
        return new EventRequestResponse(saved,"Event request created successfully");
    }

    @Override
    public Page<EventRequestResponse> getMyRequests(Long clientId, Pageable pageable) {
        return eventRequestRepository.findByClientId(clientId, pageable)
                .map(EventRequestResponse::new);
    }

    @Override
    public EventRequestResponse cancel(Long eventId, Long clientId) {
        EventRequest event = findAndValidateOwner(eventId, clientId);
        if (event.getStatus() != EventRequestStatus.OPEN)
            throw new RuntimeException("Only open events can be cancelled");
        event.setStatus(EventRequestStatus.CANCELLED);
        return new EventRequestResponse(eventRequestRepository.save(event),"Event request cancelled successfully");
    }

    @Override
    public Page<EventRequestResponse> getOpenRequests(Pageable pageable) {
        return eventRequestRepository.findByStatus(EventRequestStatus.OPEN, pageable)
                .map(EventRequestResponse::new);
    }

    @Override
    public Page<EventRequestResponse> getOpenRequestsByType(EventType type, Pageable pageable) {
        return eventRequestRepository.findByEventTypeAndStatus(type, EventRequestStatus.OPEN, pageable)
                .map(EventRequestResponse::new);
    }

    @Override
    public Page<EventRequestResponse> getAll(Pageable pageable) {
        return eventRequestRepository.findAll(pageable).map(EventRequestResponse::new);
    }

    @Override
    public void forceCancel(Long eventId) {
        EventRequest event = eventRequestRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        event.setStatus(EventRequestStatus.CANCELLED);
        eventRequestRepository.save(event);
    }

    @Override
    public EventRequest findById(Long id) {
        return eventRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event request not found"));
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private EventRequest findAndValidateOwner(Long eventId, Long clientId) {
        EventRequest event = findById(eventId);
        if (!event.getClient().getId().equals(clientId))
            throw new RuntimeException("Not authorized");
        return event;
    }
}