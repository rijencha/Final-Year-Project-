package com.example.photoGroupe.dto.rating;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhotographerRatingSummary {
    private Long photographerId;
    private double averageRating;   // e.g. 4.3
    private long totalReviews;
}