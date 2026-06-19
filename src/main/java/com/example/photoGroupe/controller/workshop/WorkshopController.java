package com.example.photoGroupe.controller.workshop;

import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.workshop.WorkshopService;
import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    // ─── Public Browsing ──────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<Page<WorkshopSummaryResponse>> list(
            @PageableDefault(size = 12, sort = "workshopDate") Pageable pageable
    ) {
        return ResponseEntity.ok(workshopService.listAvailable(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(workshopService.getWorkshop(id));
    }

    // ─── Photographer CRUD ────────────────────────────────────────────────

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<WorkshopSummaryResponse>> mine(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(workshopService.myWorkshops(currentUser));
    }

    @PostMapping
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<WorkshopDetailResponse> create(
            @RequestBody WorkshopRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws Exception {
        return ResponseEntity.ok(workshopService.createWorkshop(request, currentUser));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<WorkshopDetailResponse> update(
            @PathVariable Long id,
            @RequestBody WorkshopRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws Exception {
        return ResponseEntity.ok(workshopService.updateWorkshop(id, request, currentUser));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<WorkshopDetailResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam WorkshopStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(workshopService.updateStatus(id, status, currentUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        workshopService.deleteWorkshop(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<ParticipantResponse>> participants(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(workshopService.getParticipants(id, currentUser));
    }

    // ─── Participant: eSewa Payment ───────────────────────────────────────

}
