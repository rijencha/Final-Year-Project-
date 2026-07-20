package com.example.photoGroupe.service.report;

import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.dto.report.ReportedContentSummary;
import com.example.photoGroupe.model.Comment;
import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.report.Report;
import com.example.photoGroupe.model.report.ReportContextType;
import com.example.photoGroupe.model.report.ReportReason;
import com.example.photoGroupe.model.report.ReportStatus;
import com.example.photoGroupe.model.workshop.Workshop;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.workshop.WorkshopRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PinRepository pinRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final WorkshopRepository workshopRepository;
    private final EventRequestRepository eventRequestRepository;
    private final BidRepository bidRepository;

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

        if (request.getContextType() != null
                && request.getContextType() != ReportContextType.PROFILE
                && request.getContextId() == null) {
            throw new RuntimeException("contextId is required for this contextType");
        }

        User reportedUser = userRepository.findByIdAndDeletedFalse(reportedUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + reportedUserId));

        boolean alreadyReported;
        if (request.getContextType() != null) {
            alreadyReported = reportRepository.existsByReporterIdAndReportedUserIdAndContextTypeAndContextId(
                    reporter.getId(), reportedUserId, request.getContextType(), request.getContextId());
        } else {
            alreadyReported = reportRepository.existsByReporterIdAndReportedUserIdAndContextTypeIsNull(
                    reporter.getId(), reportedUserId);
        }

        if (alreadyReported) {
            throw new RuntimeException("You have already reported this");
        }

        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(request.getReason());
        report.setMessage(request.getMessage());
        report.setContextType(request.getContextType());
        report.setContextId(request.getContextId());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);

        List<User> admins = userRepository.findByRoleIn(List.of(Role.ADMIN, Role.SUPER_ADMIN));
        for (User admin : admins) {
            notificationService.create(
                    admin,
                    reporter,
                    "NEW_REPORT",
                    "New report filed against " + reportedUser.getActualUsername()
                            + " · Reason: " + report.getReason().getLabel(),
                    "/admin/reports/" + report.getId()
            );
        }

        return toResponse(report);
    }

    // ─── Admin: view reports ──────────────────────────────────────────────

    @Override
    public List<ReportResponse> getAllReports() {
        Map<Long, Long> countsByUser = loadReportCounts();
        return reportRepository.findAll()
                .stream()
                .map(r -> toResponse(r, countsByUser))
                .toList();
    }

    @Override
    public List<ReportResponse> getPendingReports() {
        Map<Long, Long> countsByUser = loadReportCounts();
        return reportRepository.findByStatus(ReportStatus.PENDING)
                .stream()
                .map(r -> toResponse(r, countsByUser))
                .toList();
    }

    @Override
    public List<ReportResponse> getReportsByUser(Long userId) {
        Map<Long, Long> countsByUser = loadReportCounts();
        return reportRepository.findByReportedUserId(userId)
                .stream()
                .map(r -> toResponse(r, countsByUser))
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

        reportedUser.setEnabled(false);
        userRepository.save(reportedUser);

        report.setStatus(ReportStatus.RESOLVED);
        report.setReviewedBy(admin);
        report.setReviewedAt(LocalDateTime.now());
        reportRepository.save(report);

        notificationService.create(
                reportedUser,
                admin,
                "ACCOUNT_SUSPENDED",
                "Your account has been suspended following a report. " +
                        "Reason: " + report.getReason().getLabel() +
                        (report.getMessage() != null && !report.getMessage().isBlank()
                                ? " - " + report.getMessage() : "") + ". " +
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

    private Map<Long, Long> loadReportCounts() {
        return reportRepository.countGroupedByReportedUser()
                .stream()
                .collect(Collectors.toMap(
                        ReportRepository.ReportCountProjection::getUserId,
                        ReportRepository.ReportCountProjection::getTotal
                ));
    }

    // ─── Content resolver ─────────────────────────────────────────────────

    private ReportedContentSummary resolveReportedContent(Report report) {
        ReportContextType type = report.getContextType();
        Long contextId = report.getContextId();

        if (type == null || type == ReportContextType.PROFILE) {
            return null;
        }

        try {
            switch (type) {
                case PIN -> {
                    Pin pin = pinRepository.findById(contextId).orElse(null);
                    if (pin == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            pin.getTitle() != null ? pin.getTitle() : "(untitled pin)",
                            pin.getDescription(),
                            pin.getImageUrl(),
                            "By " + pin.getUser().getActualUsername()
                                    + (pin.isSuspended() ? " · already suspended" : "")
                    );
                }
                case COMMENT -> {
                    Comment comment = commentRepository.findById(contextId).orElse(null);
                    if (comment == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            "Comment on pin: " + (comment.getPin() != null ? comment.getPin().getTitle() : "(unknown pin)"),
                            comment.getText(),
                            comment.getPin() != null ? comment.getPin().getImageUrl() : null,
                            "By " + comment.getUser().getActualUsername()
                                    + (comment.isDeleted() ? " · already deleted" : "")
                    );
                }
                case BOOKING -> {
                    Booking booking = bookingRepository.findById(contextId).orElse(null);
                    if (booking == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            booking.getEventTitle(),
                            booking.getDescription() != null ? booking.getDescription() : booking.getSpecialRequests(),
                            null,
                            "Event date: " + booking.getEventDate() + " · Status: " + booking.getStatus()
                    );
                }
                case WORKSHOP -> {
                    Workshop workshop = workshopRepository.findById(contextId).orElse(null);
                    if (workshop == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            workshop.getTitle(),
                            workshop.getDescription(),
                            workshop.getCoverImage(),
                            "Hosted by " + workshop.getPhotographer().getActualUsername()
                                    + " · " + workshop.getWorkshopDate()
                    );
                }
                case EVENT -> {
                    EventRequest event = eventRequestRepository.findById(contextId).orElse(null);
                    if (event == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            event.getTitle(),
                            event.getDescription(),
                            null,
                            "Budget: " + event.getBudgetMin() + " - " + event.getBudgetMax()
                                    + " · Status: " + event.getStatus()
                    );
                }
                case BID -> {
                    Bid bid = bidRepository.findById(contextId).orElse(null);
                    if (bid == null) return notFound();
                    return new ReportedContentSummary(
                            true,
                            "Bid on: " + (bid.getEventRequest() != null ? bid.getEventRequest().getTitle() : "(unknown event)"),
                            bid.getProposal(),
                            null,
                            "By " + bid.getPhotographer().getActualUsername()
                                    + " · Price: " + bid.getPrice() + " · Status: " + bid.getStatus()
                    );
                }
                default -> {
                    return null;
                }
            }
        } catch (Exception e) {
            return notFound();
        }
    }

    private ReportedContentSummary notFound() {
        return new ReportedContentSummary(false, "Content no longer available", null, null, null);
    }

    // ─── Response builders ────────────────────────────────────────────────

    // Used by list endpoints — batched count map (no N+1)
    private ReportResponse toResponse(Report report, Map<Long, Long> countsByUser) {
        long total = countsByUser.getOrDefault(report.getReportedUser().getId(), 0L);
        return buildResponse(report, total);
    }

    // Used by single-report endpoints (create/suspend/dismiss) — one row, cheap single count
    private ReportResponse toResponse(Report report) {
        long total = reportRepository.countByReportedUserId(report.getReportedUser().getId());
        return buildResponse(report, total);
    }

    private ReportResponse buildResponse(Report report, long totalReportsForUser) {
        return ReportResponse.builder()
                .id(report.getId())
                .reportedUserId(report.getReportedUser().getId())
                .reportedUsername(report.getReportedUser().getActualUsername())
                .reportedUserEmail(report.getReportedUser().getEmail())
                .reportedUserProfilePic(report.getReportedUser().getProfilePicture())
                .reportedUserTotalReports(totalReportsForUser)
                .reporterId(report.getReporter().getId())
                .reporterUsername(report.getReporter().getActualUsername())
                .reason(report.getReason())
                .reasonLabel(report.getReason().getLabel())
                .message(report.getMessage())
                .contextType(report.getContextType())
                .contextId(report.getContextId())
                .reportedContent(resolveReportedContent(report))
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .reviewedBy(report.getReviewedBy() != null ? report.getReviewedBy().getActualUsername() : null)
                .build();
    }
}