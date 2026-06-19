package com.example.photoGroupe.controller.booking;

import com.example.photoGroupe.dto.booking.PackageRequest;
import com.example.photoGroupe.dto.booking.PackageResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.booking.BookingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/users/packages")
@RequiredArgsConstructor
public class BookingPackageController {

    private final BookingServiceImpl packageService;

    @PostMapping
    public ResponseEntity<PackageResponse> send(
            @RequestBody PackageRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.sendPackage(req, currentUser.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        packageService.deletePackage(id, currentUser.getUser());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<PackageResponse> accept(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.acceptPackage(id, currentUser.getUser()));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<PackageResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.rejectPackage(id, currentUser.getUser()));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PackageResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(packageService.getByBookingId(bookingId));
    }

    @PatchMapping("/{id}/counter")
    public ResponseEntity<PackageResponse> counter(
            @PathVariable Long id,
            @RequestParam BigDecimal counterPrice,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.counterOffer(id, counterPrice, currentUser.getUser()));
    }

    @PatchMapping("/{id}/accept-counter")
    public ResponseEntity<PackageResponse> acceptCounter(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.acceptCounter(id, currentUser.getUser()));
    }

    @PatchMapping("/{id}/re-counter")
    public ResponseEntity<PackageResponse> reCounter(
            @PathVariable Long id,
            @RequestParam BigDecimal newPrice,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(packageService.reCounter(id, newPrice, currentUser.getUser()));
    }
}
