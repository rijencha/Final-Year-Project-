package com.example.photoGroupe.controller.admin;

import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    // GET /api/admin/reports
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    // GET /api/admin/reports/pending
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    // GET /api/admin/reports/user/{userId}
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ReportResponse>> getReportsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reportService.getReportsByUser(userId));
    }

    // POST /api/admin/reports/{reportId}/suspend
    @PostMapping("/{reportId}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ReportResponse> suspendFromReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User admin = userDetails.getUser();
        return ResponseEntity.ok(reportService.suspendFromReport(reportId, admin));
    }

    // POST /api/admin/reports/{reportId}/dismiss
    @PostMapping("/{reportId}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ReportResponse> dismissReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User admin = userDetails.getUser();
        return ResponseEntity.ok(reportService.dismissReport(reportId, admin));
    }
}