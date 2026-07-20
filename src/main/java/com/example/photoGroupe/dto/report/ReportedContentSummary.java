package com.example.photoGroupe.dto.report;

public class ReportedContentSummary {
    private boolean exists;      // false if the content was already deleted
    private String title;        // short label, e.g. "Pin: Sunset over Patan"
    private String text;         // the actual reported text (comment body, bid message, etc.)
    private String imageUrl;     // if the content has an associated image
    private String extraInfo;    // e.g. booking date, workshop title, event status

    public ReportedContentSummary() {}
    public ReportedContentSummary(boolean exists, String title, String text, String imageUrl, String extraInfo) {
        this.exists = exists; this.title = title; this.text = text;
        this.imageUrl = imageUrl; this.extraInfo = extraInfo;
    }
    public boolean isExists() { return exists; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getImageUrl() { return imageUrl; }
    public String getExtraInfo() { return extraInfo; }
}