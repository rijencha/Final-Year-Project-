package com.example.photoGroupe.model.report;

public enum ReportReason {
    SPAM("Spam"),
    FAKE_PROFILE("Fake Profile"),
    INAPPROPRIATE_CONTENT("Inappropriate Content"),
    HARASSMENT("Harassment"),
    COPYRIGHT_VIOLATION("Copyright Violation"),
    SCAM_FRAUD("Scam / Fraud"),
    OFFENSIVE_BEHAVIOR("Offensive Behavior"),
    OTHER("Other");

    private final String label;

    ReportReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}