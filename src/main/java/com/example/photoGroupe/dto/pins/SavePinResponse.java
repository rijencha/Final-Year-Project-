package com.example.photoGroupe.dto.pins;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavePinResponse {
    private Long    pinId;
    private boolean saved;           // true = just saved, false = just unsaved
    private long    totalSaves;
}