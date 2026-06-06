package com.example.photoGroupe.model.report;

public enum ReportStatus {
    PENDING,      // filed, not yet reviewed
    RESOLVED,     // admin took action (suspended)
    DISMISSED     // admin reviewed and dismissed
}
