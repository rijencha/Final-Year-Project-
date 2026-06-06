package com.example.photoGroupe.dto.report;

import com.example.photoGroupe.model.report.ReportReason;
import com.example.photoGroupe.model.report.ReportStatus;

import java.time.LocalDateTime;

public class ReportResponse {

    private Long id;
    private Long reportedUserId;
    private String reportedUsername;
    private Long reporterId;
    private String reporterUsername;
    private ReportReason reason;
    private String message;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ReportResponse r = new ReportResponse();
        public Builder id(Long v)                   { r.id = v; return this; }
        public Builder reportedUserId(Long v)        { r.reportedUserId = v; return this; }
        public Builder reportedUsername(String v)    { r.reportedUsername = v; return this; }
        public Builder reporterId(Long v)            { r.reporterId = v; return this; }
        public Builder reporterUsername(String v)    { r.reporterUsername = v; return this; }
        public Builder reason(ReportReason v)        { r.reason = v; return this; }
        public Builder message(String v)             { r.message = v; return this; }
        public Builder status(ReportStatus v)        { r.status = v; return this; }
        public Builder createdAt(LocalDateTime v)    { r.createdAt = v; return this; }
        public Builder reviewedAt(LocalDateTime v)   { r.reviewedAt = v; return this; }
        public Builder reviewedBy(String v)          { r.reviewedBy = v; return this; }
        public ReportResponse build()                { return r; }
    }

    // Getters
    public Long getId()                 { return id; }
    public Long getReportedUserId()     { return reportedUserId; }
    public String getReportedUsername() { return reportedUsername; }
    public Long getReporterId()         { return reporterId; }
    public String getReporterUsername() { return reporterUsername; }
    public ReportReason getReason()     { return reason; }
    public String getMessage()          { return message; }
    public ReportStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt(){ return reviewedAt; }
    public String getReviewedBy()       { return reviewedBy; }
}
