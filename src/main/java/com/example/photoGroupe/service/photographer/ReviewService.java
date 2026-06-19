package com.example.photoGroupe.service.photographer;

import com.example.photoGroupe.dto.rating.PhotographerRatingSummary;
import com.example.photoGroupe.dto.rating.ReviewRequest;
import com.example.photoGroupe.dto.rating.ReviewResponse;
import org.springframework.data.domain.Page;

public interface ReviewService {
    ReviewResponse addReview(Long photographerId, ReviewRequest request, Long currentUserId);
    ReviewResponse updateReview(Long reviewId, ReviewRequest request, Long currentUserId);
    ReviewResponse  deleteReview(Long reviewId, Long currentUserId);
    Page<ReviewResponse> getReviews(Long photographerId, int page, int size);
    ReviewResponse deleteRating(Long reviewId, Long currentUserId);
    PhotographerRatingSummary getRatingSummary(Long photographerId);
}
