package com.example.photoGroupe.dto.report;

import com.example.photoGroupe.model.report.ReportContextType;
import com.example.photoGroupe.model.report.ReportReason;
import com.example.photoGroupe.model.report.ReportStatus;

import java.time.LocalDateTime;

public class ReportResponse {

    private Long id;
    private Long reportedUserId;
    private String reportedUsername;
    private String reportedUserEmail;
    private String reportedUserProfilePic;
    private long reportedUserTotalReports;
    private ReportedContentSummary reportedContent;
    private Long reporterId;
    private String reporterUsername;
    private ReportReason reason;
    private String reasonLabel;
    private String message;
    private ReportContextType contextType;
    private Long contextId;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ReportResponse r = new ReportResponse();
        public Builder id(Long v)                        { r.id = v; return this; }
        public Builder reportedUserId(Long v)             { r.reportedUserId = v; return this; }
        public Builder reportedUsername(String v)         { r.reportedUsername = v; return this; }
        public Builder reportedUserEmail(String v)        { r.reportedUserEmail = v; return this; }
        public Builder reportedUserProfilePic(String v)   { r.reportedUserProfilePic = v; return this; }
        public Builder reportedUserTotalReports(long v)   { r.reportedUserTotalReports = v; return this; }
        public Builder reportedContent(ReportedContentSummary v) { r.reportedContent = v; return this; }
        public Builder reporterId(Long v)                 { r.reporterId = v; return this; }
        public Builder reporterUsername(String v)         { r.reporterUsername = v; return this; }
        public Builder reason(ReportReason v)             { r.reason = v; return this; }
        public Builder reasonLabel(String v)              { r.reasonLabel = v; return this; }
        public Builder message(String v)                  { r.message = v; return this; }
        public Builder contextType(ReportContextType v)   { r.contextType = v; return this; }
        public Builder contextId(Long v)                  { r.contextId = v; return this; }
        public Builder status(ReportStatus v)             { r.status = v; return this; }
        public Builder createdAt(LocalDateTime v)         { r.createdAt = v; return this; }
        public Builder reviewedAt(LocalDateTime v)        { r.reviewedAt = v; return this; }
        public Builder reviewedBy(String v)               { r.reviewedBy = v; return this; }
        public ReportResponse build()                     { return r; }
    }

    // Getters
    public Long getId()                       { return id; }
    public Long getReportedUserId()           { return reportedUserId; }
    public String getReportedUsername()       { return reportedUsername; }
    public String getReportedUserEmail()      { return reportedUserEmail; }
    public String getReportedUserProfilePic() { return reportedUserProfilePic; }
    public long getReportedUserTotalReports() { return reportedUserTotalReports; }
    public ReportedContentSummary getReportedContent() { return reportedContent; }
    public Long getReporterId()               { return reporterId; }
    public String getReporterUsername()       { return reporterUsername; }
    public ReportReason getReason()           { return reason; }
    public String getReasonLabel()            { return reasonLabel; }
    public String getMessage()                { return message; }
    public ReportContextType getContextType() { return contextType; }
    public Long getContextId()                { return contextId; }
    public ReportStatus getStatus()           { return status; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getReviewedAt()      { return reviewedAt; }
    public String getReviewedBy()             { return reviewedBy; }
}