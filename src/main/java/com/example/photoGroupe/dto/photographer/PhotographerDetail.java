package com.example.photoGroupe.dto.photographer;

import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.rating.ReviewResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PhotographerDetail {
    private Long id;
    private String fullName;
    private String username;
    private String bio;
    private String location;
    private String profilePicture;
    private Integer yearsOfExperience;
    private List<SpecializationResponse> specializations;
    private boolean verified;
    private boolean enable;
    private boolean accountNonLocked;   // ✅ add (ban/suspend)
    private boolean deleted;
    private LocalDateTime joinedAt;
    private String phoneNumber;
    private long pinCount;
    private double averageRating;
    private List<ReviewResponse> recentReviews;
    private long reviewCount;   // total reviews with a comment
    private long ratingCount;   // total entries (rating or comment)
}

