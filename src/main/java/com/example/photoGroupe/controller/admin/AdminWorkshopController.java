package com.example.photoGroupe.controller.admin;

import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.workshop.WorkshopService;
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
@RequestMapping("/api/admin/workshops")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminWorkshopController {

    private final WorkshopService workshopService;

    @GetMapping
    public ResponseEntity<Page<WorkshopSummaryResponse>> listAll(
            @PageableDefault(size = 12, sort = "workshopDate") Pageable pageable) {
        return ResponseEntity.ok(workshopService.listAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(workshopService.getWorkshop(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<WorkshopDetailResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam WorkshopStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                workshopService.adminUpdateStatus(id, status, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        workshopService.adminDeleteWorkshop(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantResponse>> participants(@PathVariable Long id) {
        return ResponseEntity.ok(workshopService.adminGetParticipants(id));
    }
}
