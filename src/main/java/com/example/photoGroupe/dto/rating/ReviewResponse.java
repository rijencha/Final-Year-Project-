package com.example.photoGroupe.dto.rating;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponse {
    private Long id;
    private int rating;
    private String comment;
    private boolean edited;

    private Long reviewerId;
    private String reviewerUsername;
    private String reviewerProfilePicture;

    private Long photographerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}