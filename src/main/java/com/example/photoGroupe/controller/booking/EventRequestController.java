package com.example.photoGroupe.controller.booking;

import com.example.photoGroupe.dto.eventandbid.EventRequestDTO;
import com.example.photoGroupe.dto.eventandbid.EventRequestResponse;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.event.EventRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/events")
@RequiredArgsConstructor
public class EventRequestController {
    private final EventRequestService eventRequestService;

    // Client — create event request
    @PostMapping
    public ResponseEntity<EventRequestResponse> create(
            @RequestBody EventRequestDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(eventRequestService.create(currentUser.getUser(), dto));
    }

    // Client — my event requests
    @GetMapping("/my")
    public ResponseEntity<Page<EventRequestResponse>> myRequests(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable
    ) {
        return ResponseEntity.ok(eventRequestService.getMyRequests(currentUser.getId(), pageable));
    }

    // Client — cancel event
    @PutMapping("/{id}/cancel")
    public ResponseEntity<EventRequestResponse> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(eventRequestService.cancel(id, currentUser.getId()));
    }

    // Photographer — browse open events
    @GetMapping("/open")
    public ResponseEntity<Page<EventRequestResponse>> getOpen(Pageable pageable) {
        return ResponseEntity.ok(eventRequestService.getOpenRequests(pageable));
    }

    // Photographer — filter by event type
    @GetMapping("/open/type/{type}")
    public ResponseEntity<Page<EventRequestResponse>> getOpenByType(
            @PathVariable EventType type, Pageable pageable
    ) {
        return ResponseEntity.ok(eventRequestService.getOpenRequestsByType(type, pageable));
    }

    // Admin — get all
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Page<EventRequestResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(eventRequestService.getAll(pageable));
    }

    // Admin — force cancel
    @PutMapping("/admin/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> forceCancel(@PathVariable Long id) {
        eventRequestService.forceCancel(id);
        return ResponseEntity.ok().build();
    }
}
