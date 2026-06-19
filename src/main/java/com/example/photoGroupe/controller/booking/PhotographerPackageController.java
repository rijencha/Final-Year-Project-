package com.example.photoGroupe.controller.booking;

import com.example.photoGroupe.dto.booking.PhotographerPackageRequest;
import com.example.photoGroupe.dto.booking.PhotographerPackageResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.booking.PhotographerPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/photographer-packages")
@RequiredArgsConstructor
public class PhotographerPackageController {

    private final PhotographerPackageService service;

    @PostMapping
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerPackageResponse> create(
            @RequestBody PhotographerPackageRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(service.create(req, currentUser.getUser()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<PhotographerPackageResponse> update(
            @PathVariable Long id,
            @RequestBody PhotographerPackageRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(service.update(id, req, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        service.delete(id, currentUser.getUser());
        return ResponseEntity.ok().build();
    }

    // Photographer sees their own templates
    @GetMapping("/my")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<List<PhotographerPackageResponse>> myTemplates(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(service.getMyTemplates(currentUser.getUser()));
    }

    // Client browses a photographer's packages before/during booking
    @GetMapping("/photographer/{photographerId}")
    public ResponseEntity<List<PhotographerPackageResponse>> byPhotographer(
            @PathVariable Long photographerId) {
        return ResponseEntity.ok(service.getByPhotographerId(photographerId));
    }
}