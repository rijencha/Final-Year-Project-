package com.example.photoGroupe.dto.admin;

public class AdminForceCancelRequest {
    private String reason;
    private String penalizeParty; // "CLIENT", "PHOTOGRAPHER", "BOTH", or "NONE" (default NONE)
    private Integer penaltyDays;  // defaults to 10 if not provided

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPenalizeParty() { return penalizeParty; }
    public void setPenalizeParty(String penalizeParty) { this.penalizeParty = penalizeParty; }
    public Integer getPenaltyDays() { return penaltyDays; }
    public void setPenaltyDays(Integer penaltyDays) { this.penaltyDays = penaltyDays; }
}