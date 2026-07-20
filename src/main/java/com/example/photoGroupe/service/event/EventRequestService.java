package com.example.photoGroupe.service.event;


import com.example.photoGroupe.dto.eventandbid.EventRequestDTO;
import com.example.photoGroupe.dto.eventandbid.EventRequestResponse;
import com.example.photoGroupe.dto.share.ShareResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRequestService {
    EventRequestResponse create(User client, EventRequestDTO dto);
    Page<EventRequestResponse> getMyRequests(Long clientId, Pageable pageable);
    EventRequestResponse cancel(Long eventId, Long clientId);
    Page<EventRequestResponse> getOpenRequests(Pageable pageable);
    Page<EventRequestResponse> getOpenRequestsByType(EventType type, Pageable pageable);
    Page<EventRequestResponse> getAll(Pageable pageable);
    void forceCancel(Long eventId);
    EventRequest findById(Long id);
    Page<EventRequestResponse> getOpenRequestsByCustomType(String customType, Pageable pageable);
    ShareResponse shareEvent(Long eventId, User sharer);
}
