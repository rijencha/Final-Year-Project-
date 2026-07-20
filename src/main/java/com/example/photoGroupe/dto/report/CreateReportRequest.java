package com.example.photoGroupe.dto.report;

import com.example.photoGroupe.model.report.ReportContextType;
import com.example.photoGroupe.model.report.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateReportRequest {
    @NotNull(message = "Reason is required")
    private ReportReason reason;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message; // optional unless reason == OTHER

    private ReportContextType contextType; // optional, null = general profile report
    private Long contextId;

    public ReportReason getReason()             { return reason; }
    public String getMessage()                  { return message; }
    public void setReason(ReportReason reason)  { this.reason = reason; }
    public void setMessage(String message)      { this.message = message; }
    public ReportContextType getContextType() {
        return contextType;
    }

    public void setContextType(ReportContextType contextType) {
        this.contextType = contextType;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }
}