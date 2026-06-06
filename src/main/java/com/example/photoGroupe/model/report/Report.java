package com.example.photoGroupe.model.report;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    // The user who filed the report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    // Free-text message from the reporter (required for OTHER, optional for rest)
    @Column(name = "message", length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    // Set when admin acts on this report
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public User getReportedUser()               { return reportedUser; }
    public User getReporter()                   { return reporter; }
    public ReportReason getReason()             { return reason; }
    public String getMessage()                  { return message; }
    public ReportStatus getStatus()             { return status; }
    public User getReviewedBy()                 { return reviewedBy; }
    public LocalDateTime getReviewedAt()        { return reviewedAt; }
    public LocalDateTime getCreatedAt()         { return createdAt; }

    public void setReportedUser(User u)         { this.reportedUser = u; }
    public void setReporter(User u)             { this.reporter = u; }
    public void setReason(ReportReason r)       { this.reason = r; }
    public void setMessage(String m)            { this.message = m; }
    public void setStatus(ReportStatus s)       { this.status = s; }
    public void setReviewedBy(User u)           { this.reviewedBy = u; }
    public void setReviewedAt(LocalDateTime t)  { this.reviewedAt = t; }
}