package com.example.photoGroupe.dto.photographer;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PhotographerVerificationResponse {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String portfolioLink;
    private String bio;
    private String location;
    private String verificationStatus;
    private LocalDateTime joinedAt;
    private String phoneNumber;
    private String profilePicture;
    private long pinCount;
    private long ratingCount;
    private long reviewCount;
    private double averageRating;
}
