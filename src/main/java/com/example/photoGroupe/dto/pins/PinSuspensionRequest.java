package com.example.photoGroupe.dto.pins;

public class PinSuspensionRequest {
    private String reason;  // "Nudity", "Spam", "Hate speech", etc.
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
