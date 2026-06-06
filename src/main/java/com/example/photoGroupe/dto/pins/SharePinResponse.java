package com.example.photoGroupe.dto.pins;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SharePinResponse {
    private Long   pinId;
    private String shareLink;
    private long   totalShares;
}