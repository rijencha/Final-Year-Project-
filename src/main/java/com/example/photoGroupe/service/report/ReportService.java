package com.example.photoGroupe.service.report;

import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.model.User;

import java.util.List;

public interface ReportService {
    // User-facing
    ReportResponse createReport(Long reportedUserId, CreateReportRequest request, User reporter);

    // Admin-facing
    List<ReportResponse> getAllReports();
    List<ReportResponse> getPendingReports();
    List<ReportResponse> getReportsByUser(Long userId);
    ReportResponse suspendFromReport(Long reportId, User admin);
    ReportResponse dismissReport(Long reportId, User admin);
}
