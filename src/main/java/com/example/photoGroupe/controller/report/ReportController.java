package com.example.photoGroupe.controller.report;

import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // ─── User: file a report ───────────────────────────────────────────────

    @PostMapping("/{reportedUserId}")
    public ReportResponse createReport(
            @PathVariable Long reportedUserId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return reportService.createReport(reportedUserId, request, currentUser.getUser());
    }

    // ─── Admin: view reports ───────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public List<ReportResponse> getAllReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public List<ReportResponse> getPendingReports() {
        return reportService.getPendingReports();
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public List<ReportResponse> getReportsByUser(@PathVariable Long userId) {
        return reportService.getReportsByUser(userId);
    }

    // ─── Admin: act on report ───────────────────────────────────────────────

    @PutMapping("/{reportId}/suspend")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ReportResponse suspendFromReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return reportService.suspendFromReport(reportId, currentUser.getUser());
    }

    @PutMapping("/{reportId}/dismiss")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ReportResponse dismissReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return reportService.dismissReport(reportId, currentUser.getUser());
    }
}