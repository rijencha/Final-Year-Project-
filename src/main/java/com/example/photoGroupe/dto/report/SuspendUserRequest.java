package com.example.photoGroupe.dto.report;

import com.example.photoGroupe.model.report.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SuspendUserRequest {
    @NotNull(message = "Report reason is required")
    private ReportReason reason;

    // Required only when reason == OTHER
    @Size(max = 500, message = "Custom reason must not exceed 500 characters")
    private String customReason;

    public ReportReason getReason()             { return reason; }
    public String getCustomReason()             { return customReason; }
    public void setReason(ReportReason reason)  { this.reason = reason; }
    public void setCustomReason(String c)       { this.customReason = c; }
}
