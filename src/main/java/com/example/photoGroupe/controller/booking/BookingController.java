package com.example.photoGroupe.controller.booking;


import com.example.photoGroupe.dto.booking.BookingRequest;
import com.example.photoGroupe.dto.booking.BookingResponse;
import com.example.photoGroupe.dto.booking.BookingStatusRequest;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.booking.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ── Client ───────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @RequestBody @Valid BookingRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.create(currentUser.getUser(), req));
    }

    @GetMapping("/my")
    public ResponseEntity<Page<BookingResponse>> myBookings(
            @RequestParam(required = false) BookingStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getClientBookings(currentUser.getId(), status, pageable));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(
            @PathVariable Long id,
            @RequestBody(required = false) BookingStatusRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        String reason = req != null ? req.getReason() : null;
        return ResponseEntity.ok(bookingService.cancel(id, currentUser.getUser(), reason));
    }

    // ── Photographer ─────────────────────────────────────────────────────

    @GetMapping("/photographer")
    @PreAuthorize("hasRole('PHOTOGRAPHER')")
    public ResponseEntity<Page<BookingResponse>> photographerBookings(
            @RequestParam(required = false) BookingStatus status,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getPhotographerBookings(currentUser.getId(), status, pageable));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<BookingResponse> confirm(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.confirm(id, currentUser.getUser()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) BookingStatusRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        String reason = req != null ? req.getReason() : null;
        return ResponseEntity.ok(bookingService.reject(id, currentUser.getUser(), reason));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<BookingResponse> complete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.complete(id, currentUser.getUser()));
    }

    @GetMapping("/photographer/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.getPhotographerStats(currentUser.getId()));
    }

    // ── Shared ───────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(bookingService.getById(id));
    }

    // Client confirms completion and releases payment
    @PostMapping("/{id}/release-payment")
    public ResponseEntity<BookingResponse> releasePayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                bookingService.releasePayment(id, currentUser.getUser())
        );
    }


    // ── Admin ─────────────────────────────────────────────────────────────

    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Page<BookingResponse>> getAll(
            @RequestParam(required = false) BookingStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookingService.getAll(status, pageable));
    }
}
