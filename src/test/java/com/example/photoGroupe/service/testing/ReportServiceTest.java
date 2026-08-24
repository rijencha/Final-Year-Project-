package com.example.photoGroupe.service.testing;

import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.report.Report;
import com.example.photoGroupe.model.report.ReportReason;
import com.example.photoGroupe.model.report.ReportStatus;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.workshop.WorkshopRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.report.ReportServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private ReportRepository reportRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private ReportServiceImpl reportService;

    private User reporter;
    private User reportedUser;
    private CreateReportRequest req;

    @BeforeEach
    void setUp() {
        reporter = new User();
        reporter.setId(1L);

        reportedUser = new User();
        reportedUser.setId(2L);

        req = new CreateReportRequest();
        req.setReason(ReportReason.SPAM);
    }

    @Test
    void createReport_throwsWhenReportingYourself() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> reportService.createReport(1L, req, reporter));
        assertEquals("You cannot report yourself", ex.getMessage());
    }

    @Test
    void createReport_requiresMessageWhenReasonIsOther() {
        req.setReason(ReportReason.OTHER);
        req.setMessage(null);

        assertThrows(RuntimeException.class,
                () -> reportService.createReport(2L, req, reporter));
    }

    @Test
    void createReport_throwsWhenUserNotFound() {
        when(userRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reportService.createReport(2L, req, reporter));
    }

    @Test
    void createReport_throwsWhenAlreadyReportedSameContext() {
        when(userRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(reportedUser));
        when(reportRepository.existsByReporterIdAndReportedUserIdAndContextTypeIsNull(1L, 2L))
                .thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> reportService.createReport(2L, req, reporter));
    }

    @Test
    void createReport_notifiesAllAdmins() {
        when(userRepository.findByIdAndDeletedFalse(2L)).thenReturn(Optional.of(reportedUser));
        when(reportRepository.existsByReporterIdAndReportedUserIdAndContextTypeIsNull(1L, 2L))
                .thenReturn(false);
        when(reportRepository.countByReportedUserId(2L)).thenReturn(0L);

        User admin1 = new User(); admin1.setId(10L); admin1.setRole(Role.ADMIN);
        User admin2 = new User(); admin2.setId(11L); admin2.setRole(Role.SUPER_ADMIN);
        when(userRepository.findByRoleIn(List.of(Role.ADMIN, Role.SUPER_ADMIN)))
                .thenReturn(List.of(admin1, admin2));

        reportService.createReport(2L, req, reporter);

        verify(notificationService, times(2))
                .create(any(User.class), eq(reporter), eq("NEW_REPORT"), anyString(), anyString());
    }

    @Test
    void suspendFromReport_throwsWhenReportAlreadyResolved() {
        Report report = reportWithStatus(ReportStatus.RESOLVED);
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));

        User admin = new User(); admin.setId(99L);

        assertThrows(RuntimeException.class,
                () -> reportService.suspendFromReport(5L, admin));
    }

    @Test
    void suspendFromReport_throwsWhenUserAlreadySuspended() {
        Report report = reportWithStatus(ReportStatus.PENDING);
        report.getReportedUser().setEnabled(false);
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));

        User admin = new User(); admin.setId(99L);

        assertThrows(RuntimeException.class,
                () -> reportService.suspendFromReport(5L, admin));
    }

    @Test
    void suspendFromReport_disablesUserAndResolvesReport() {
        Report report = reportWithStatus(ReportStatus.PENDING);
        report.getReportedUser().setEnabled(true);
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));
        when(reportRepository.countByReportedUserId(anyLong())).thenReturn(1L);

        User admin = new User(); admin.setId(99L);

        reportService.suspendFromReport(5L, admin);

        assertFalse(report.getReportedUser().isEnabled());
        assertEquals(ReportStatus.RESOLVED, report.getStatus());
        assertEquals(admin, report.getReviewedBy());
    }

    @Test
    void dismissReport_throwsWhenNotPending() {
        Report report = reportWithStatus(ReportStatus.DISMISSED);
        when(reportRepository.findById(5L)).thenReturn(Optional.of(report));

        assertThrows(RuntimeException.class,
                () -> reportService.dismissReport(5L, new User()));
    }

    // ── helpers ───────────────────────────────────────────────

    private Report reportWithStatus(ReportStatus status) {
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(ReportReason.SPAM);
        report.setStatus(status);
        return report;
    }
}