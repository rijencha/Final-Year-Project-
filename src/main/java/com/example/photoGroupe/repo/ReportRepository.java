package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.report.Report;
import com.example.photoGroupe.model.report.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReportedUserId(Long userId);
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);
    boolean existsByReporterIdAndReportedUserId(Long reporterId, Long reportedUserId);
}
