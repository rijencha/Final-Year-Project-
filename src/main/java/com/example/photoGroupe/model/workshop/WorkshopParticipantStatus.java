package com.example.photoGroupe.model.workshop;

public enum WorkshopParticipantStatus {
    PENDING_PAYMENT,   // registered but not yet paid via eSewa
    CONFIRMED,         // payment verified, seat locked
    CANCELLED,         // participant withdrew or payment failed
    REFUNDED           // payment returned (e.g. workshop cancelled)
}