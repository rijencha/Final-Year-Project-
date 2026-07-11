package com.example.photoGroupe.dto.album;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadResponse {
    private Long id;           // pin or album id
    private String downloadUrl;
    private long totalDownloads;
}