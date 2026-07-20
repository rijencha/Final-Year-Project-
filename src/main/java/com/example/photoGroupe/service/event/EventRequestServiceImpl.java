package com.example.photoGroupe.service.event;

import com.example.photoGroupe.dto.eventandbid.EventRequestDTO;
import com.example.photoGroupe.dto.eventandbid.EventRequestResponse;
import com.example.photoGroupe.dto.share.ShareResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.model.share.ShareableType;
import com.example.photoGroupe.repo.EventRequestRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.share.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {

    private final EventRequestRepository eventRequestRepository;
    private final NotificationService notificationService;
    private final ShareService shareService;

    @Override
    public EventRequestResponse create(User client, EventRequestDTO dto) {
        if (dto.getEventType() == null && (dto.getCustomEventType() == null || dto.getCustomEventType().isBlank()))
            throw new RuntimeException("Provide either eventType or customEventType");

        // If a custom type is provided and no standard type matched, use OTHER as the enum anchor
        EventType resolvedType = dto.getEventType();
        String customType = dto.getCustomEventType();

        if (resolvedType == null && customType != null && !customType.isBlank()) {
            resolvedType = EventType.OTHER;  // anchor for custom types
        }

        EventRequest event = EventRequest.builder()
                .client(client)
                .title(dto.getTitle())
                .eventType(resolvedType)
                .customEventType(customType)
                .eventDate(dto.getEventDate())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .budgetMin(dto.getBudgetMin())
                .budgetMax(dto.getBudgetMax())
                .deadlineAt(dto.getDeadlineAt())
                .build();

        EventRequest saved = eventRequestRepository.save(event);
        return new EventRequestResponse(saved, "Event request created successfully");
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

    @Override
    public Page<EventRequestResponse> getOpenRequestsByCustomType(String customType, Pageable pageable) {
        return eventRequestRepository
                .findByCustomEventTypeIgnoreCaseAndStatus(customType, EventRequestStatus.OPEN, pageable)
                .map(EventRequestResponse::new);
    }

    @Override
    public ShareResponse shareEvent(Long eventId, User sharer) {
        EventRequest event = findById(eventId);

        ShareResponse response = shareService.share(ShareableType.EVENT, eventId, "event", sharer);

        if (!event.getClient().getId().equals(sharer.getId())) {
            notificationService.create(
                    event.getClient(),
                    sharer,
                    "EVENT_SHARED",
                    sharer.getFullName() + " shared your event request \"" + event.getTitle() + "\"",
                    "/events/" + eventId
            );
        }

        return response;
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private EventRequest findAndValidateOwner(Long eventId, Long clientId) {
        EventRequest event = findById(eventId);
        if (!event.getClient().getId().equals(clientId))
            throw new RuntimeException("Not authorized");
        return event;
    }
}