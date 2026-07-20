package com.example.photoGroupe.dto.share;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ShareResponse {
    private Long entityId;
    private String entityType;
    private String shareLink;
    private long totalShares;
}