package com.example.photoGroupe.model.event;

public enum EventRequestStatus {
    OPEN,       // accepting bids
    CLOSED,     // bid accepted, no more bids
    CANCELLED,  // client cancelled
    COMPLETED   // event done
}
