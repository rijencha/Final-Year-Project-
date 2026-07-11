package com.example.photoGroupe.controller.admin;

import com.example.photoGroupe.dto.admin.CreateAdminRequest;
import com.example.photoGroupe.dto.admin.UpdateAdminPasswordRequest;
import com.example.photoGroupe.dto.booking.EventTypeBookingResponse;
import com.example.photoGroupe.dto.booking.SpecializationBookingResponse;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerVerificationResponse;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // user mgmt
    @PostMapping("/create-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserSummary> createAdmin(@RequestBody CreateAdminRequest request) {
        return ResponseEntity.ok(adminService.createAdmin(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UserSummary> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UserSummary>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // delete soft delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User soft deleted successfully");
    }

    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> hardDeleteUser(@PathVariable Long id) {
        adminService.hardDeleteUser(id);
        return ResponseEntity.ok("User permanently deleted");
    }

    @PutMapping("/{id}/restore")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserSummary> restoreUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.restoreUser(id));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserSummary>> getDeletedUsers() {
        return ResponseEntity.ok(adminService.getDeletedUsers());
    }


    // ─── Photographer Verification ────────────────────────────────────────

    @GetMapping("/photographers/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PhotographerVerificationResponse>> getPendingPhotographers() {
        return ResponseEntity.ok(adminService.getPendingPhotographers());
    }

    @PutMapping("/photographers/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PhotographerVerificationResponse> approvePhotographer(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.approvePhotographer(id));
    }

    @PutMapping("/photographers/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PhotographerVerificationResponse> rejectPhotographer(
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectPhotographer(id));
    }

// one endpoint for approval and reject:
//    @PatchMapping("/{id}/verification")
//    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
//    public ResponseEntity<PhotographerVerificationResponse> updateVerification(
//            @PathVariable Long id,
//            @RequestParam VerificationStatus status) {
//        return ResponseEntity.ok(adminService.updateVerificationStatus(id, status));
//    }

    @GetMapping("/photographers")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<PhotographerVerificationResponse>> getAllPhotographers() {
        return ResponseEntity.ok(adminService.getAllPhotographers());
    }

    @GetMapping("/photographers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PhotographerVerificationResponse> getPhotographerById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getPhotographerById(id));
    }

    @GetMapping("/booking/event-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public List<EventTypeBookingResponse> getAllGroupedByEventType(
            @RequestParam(required = false) BookingStatus status) {
        return adminService.getBookingsGroupedByEventType(status);
    }

    @GetMapping("/booking/event-type/{eventTypeName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public EventTypeBookingResponse getByEventType(
            @PathVariable String eventTypeName,
            @RequestParam(required = false) BookingStatus status) {
        return adminService.getBookingsByEventType(eventTypeName, status);
    }

    // ─── Admin lifecycle management (SUPER_ADMIN only) ─────────────────────

    @PutMapping("/{adminId}/password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserSummary> updateAdminPassword(
            @PathVariable Long adminId,
            @RequestBody UpdateAdminPasswordRequest request) {
        return ResponseEntity.ok(adminService.updateAdminPassword(adminId, request.getNewPassword()));
    }

    @PutMapping("/{adminId}/revoke")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserSummary> revokeAdmin(@PathVariable Long adminId) {
        return ResponseEntity.ok(adminService.revokeAdmin(adminId));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<List<UserSummary>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

}
