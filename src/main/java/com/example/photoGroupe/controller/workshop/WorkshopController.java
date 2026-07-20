package com.example.photoGroupe.controller.workshop;

import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.workshop.WorkshopService;
import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    // ─── Public Browsing ──────────────────────────────────────────────────

//    @GetMapping
//    public ResponseEntity<Page<WorkshopSummaryResponse>> list(
//            @PageableDefault(size = 12, sort = "workshopDate") Pageable pageable
//    ) {
//        return ResponseEntity.ok(workshopService.listAvailable(pageable));
//    }

    @GetMapping
    public ResponseEntity<Page<WorkshopSummaryResponse>> list(
            @PageableDefault(size = 12, sort = "workshopDate") Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(workshopService.listAvailable(pageable, currentUserId));
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkshopDetailResponse> createWorkshop(
            @RequestPart("request") WorkshopRequest req,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(workshopService.createWorkshop(req, coverImage, currentUser));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkshopDetailResponse> updateWorkshop(
            @PathVariable Long id,
            @RequestPart("request") WorkshopRequest req,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(workshopService.updateWorkshop(id, req, coverImage, currentUser));
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

    @GetMapping("/{id}/registration-defaults")
    public ResponseEntity<WorkshopRegistrationRequest> getRegistrationDefaults(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User u = currentUser.getUser();
        return ResponseEntity.ok(new WorkshopRegistrationRequest(
                u.getFullName(), u.getEmail(), u.getPhoneNumber(), null
        ));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<Long> register(
            @PathVariable Long id,
            @RequestBody WorkshopRegistrationRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long participantId = workshopService.registerParticipant(id, req, currentUser);
        return ResponseEntity.ok(participantId); // frontend uses this to call payment initiate next
    }

    @GetMapping("/{id}/my-registration")
    public ResponseEntity<ParticipantResponse> getMyRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(workshopService.getMyRegistration(id, currentUser));
    }

}
