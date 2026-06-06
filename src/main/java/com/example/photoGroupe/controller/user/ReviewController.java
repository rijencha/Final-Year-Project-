package com.example.photoGroupe.controller.user;


import com.example.photoGroupe.dto.rating.PhotographerRatingSummary;
import com.example.photoGroupe.dto.rating.ReviewRequest;
import com.example.photoGroupe.dto.rating.ReviewResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.photographer.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // POST /api/photographers/{photographerId}/reviews
    @PostMapping("/{photographerId}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Long photographerId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reviewService.addReview(photographerId, request, userDetails.getUser().getId())
        );
    }

    // PUT /api/photographers/reviews/{reviewId}
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                reviewService.updateReview(reviewId, request, userDetails.getUser().getId())
        );
    }

    // DELETE /api/photographers/reviews/{reviewId}
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        reviewService.deleteReview(reviewId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    // GET /api/photographers/{photographerId}/reviews?page=0&size=10
    @GetMapping("/{photographerId}/reviews")
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @PathVariable Long photographerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(reviewService.getReviews(photographerId, page, size));
    }

    // GET /api/photographers/{photographerId}/rating
    @GetMapping("/{photographerId}/rating")
    public ResponseEntity<PhotographerRatingSummary> getRatingSummary(
            @PathVariable Long photographerId
    ) {
        return ResponseEntity.ok(reviewService.getRatingSummary(photographerId));
    }
}
