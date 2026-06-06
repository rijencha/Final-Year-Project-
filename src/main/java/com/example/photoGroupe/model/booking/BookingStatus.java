package com.example.photoGroupe.model.booking;

public enum BookingStatus {
    PENDING,        // user requested, waiting for photographer
    CONFIRMED,      // photographer accepted
    REJECTED,       // photographer rejected
    CANCELLED,      // user cancelled
    COMPLETED,      // event done
    NO_SHOW         // client didn't show up
}
