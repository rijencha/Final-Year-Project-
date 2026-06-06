package com.example.photoGroupe.service.report;

import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.report.Report;
import com.example.photoGroupe.model.report.ReportReason;
import com.example.photoGroupe.model.report.ReportStatus;
import com.example.photoGroupe.repo.ReportRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ─── User: file a report ──────────────────────────────────────────────

    @Override
    public ReportResponse createReport(Long reportedUserId,
                                       CreateReportRequest request,
                                       User reporter) {

        if (reporter.getId().equals(reportedUserId)) {
            throw new RuntimeException("You cannot report yourself");
        }

        if (request.getReason() == ReportReason.OTHER &&
                (request.getMessage() == null || request.getMessage().isBlank())) {
            throw new RuntimeException("A message is required when reason is OTHER");
        }

        User reportedUser = userRepository.findByIdAndDeletedFalse(reportedUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reportedUserId));

        // Prevent duplicate pending report from same reporter
        if (reportRepository.existsByReporterIdAndReportedUserId(
                reporter.getId(), reportedUserId)) {
            throw new RuntimeException("You have already reported this user");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(request.getReason());
        report.setMessage(request.getMessage());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);

        return toResponse(report);
    }

    // ─── Admin: view reports ──────────────────────────────────────────────

    @Override
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByUser(Long userId) {
        return reportRepository.findByReportedUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ─── Admin: suspend from report ───────────────────────────────────────

    @Override
    public ReportResponse suspendFromReport(Long reportId, User admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("Report is already " + report.getStatus().name());
        }

        User reportedUser = report.getReportedUser();

        if (!reportedUser.isEnabled()) {
            throw new RuntimeException("User is already suspended");
        }

        // Suspend the user
        reportedUser.setEnabled(false);
        userRepository.save(reportedUser);

        // Mark report resolved
        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        // Notify the suspended user
        String reasonLabel = buildReasonLabel(report.getReason(), report.getMessage());

        notificationService.create(
                reportedUser,
                admin,
                "ACCOUNT_SUSPENDED",
                "Your account has been suspended following a report. " +
                        "Reason: " + reasonLabel + ". " +
                        "If you believe this is a mistake, please contact support.",
                "/support"
        );

        return toResponse(report);
    }

    // ─── Admin: dismiss report ────────────────────────────────────────────

    @Override
    public ReportResponse dismissReport(Long reportId, User admin) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("Report is already " + report.getStatus().name());
        }

        report.setStatus(ReportStatus.DISMISSED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        return toResponse(report);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private String buildReasonLabel(ReportReason reason, String message) {
        return switch (reason) {
            case SPAM                  -> "Spam";
            case FAKE_PROFILE          -> "Fake Profile";
            case INAPPROPRIATE_CONTENT -> "Inappropriate Content";
            case HARASSMENT            -> "Harassment";
            case COPYRIGHT_VIOLATION   -> "Copyright Violation";
            case SCAM_FRAUD            -> "Scam / Fraud";
            case OFFENSIVE_BEHAVIOR    -> "Offensive Behavior";
            case OTHER                 -> (message != null ? message : "Other");
        };
    }

    private ReportResponse toResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportedUserId(report.getReportedUser().getId())
                .reportedUsername(report.getReportedUser().getActualUsername())
                .reporterId(report.getReporter().getId())
                .reporterUsername(report.getReporter().getActualUsername())
                .reason(report.getReason())
                .message(report.getMessage())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .reviewedBy(report.getReviewedBy() != null
                        ? report.getReviewedBy().getActualUsername()
                        : null)
                .build();
    }
}
