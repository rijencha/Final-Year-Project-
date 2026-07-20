package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.report.Report;
import com.example.photoGroupe.model.report.ReportContextType;
import com.example.photoGroupe.model.report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportedUserId(Long userId);
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);
    boolean existsByReporterIdAndReportedUserIdAndContextTypeAndContextId(
            Long reporterId, Long reportedUserId, ReportContextType contextType, Long contextId
    );

    boolean existsByReporterIdAndReportedUserIdAndContextTypeIsNull(
            Long reporterId, Long reportedUserId
    );

    long countByReportedUserId(Long reportedUserId);

    @Query("SELECT r.reportedUser.id AS userId, COUNT(r) AS total " +
            "FROM Report r GROUP BY r.reportedUser.id")
    List<ReportCountProjection> countGroupedByReportedUser();

    interface ReportCountProjection {
        Long getUserId();
        Long getTotal();
    }
}
