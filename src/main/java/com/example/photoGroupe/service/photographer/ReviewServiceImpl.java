package com.example.photoGroupe.service.photographer;

import com.example.photoGroupe.dto.rating.PhotographerRatingSummary;
import com.example.photoGroupe.dto.rating.ReviewRequest;
import com.example.photoGroupe.dto.rating.ReviewResponse;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.model.rating.PhotographerReview;
import com.example.photoGroupe.repo.PhotographerReviewRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final PhotographerReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ─── Add Review ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse addReview(Long photographerId, ReviewRequest request, Long currentUserId) {
        User photographer = findApprovedPhotographer(photographerId);
        User reviewer = findUser(currentUserId);

        // A photographer cannot review themselves
        if (currentUserId.equals(photographerId)) {
            throw new RuntimeException("You cannot review yourself");
        }

        // One review per user per photographer
        if (reviewRepository.existsByReviewerIdAndPhotographerIdAndDeletedFalse(currentUserId, photographerId)) {
            throw new RuntimeException("You have already reviewed this photographer");
        }

        PhotographerReview review = new PhotographerReview(
                request.getRating(),
                request.getComment(),
                reviewer,
                photographer
        );
        reviewRepository.save(review);

        // Notify the photographer
        notificationService.create(
                photographer,
                reviewer,
                "NEW_REVIEW",
                reviewer.getFullName() + " gave you a " + request.getRating() + "★ review",
                "/photographer/" + photographerId
        );

        return toResponse(review);
    }

    // ─── Update Review ────────────────────────────────────────────────────

    @Override
    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request, Long currentUserId) {
        PhotographerReview review = findActiveReview(reviewId);

        if (!review.getReviewer().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        return toResponse(reviewRepository.save(review));
    }

    // ─── Delete Review ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteReview(Long reviewId, Long currentUserId) {
        PhotographerReview review = findActiveReview(reviewId);

        User currentUser = findUser(currentUserId);
        boolean isOwner = review.getReviewer().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.SUPER_ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("Not authorized to delete this review");
        }

        review.setDeleted(true);
        reviewRepository.save(review);
    }

    // ─── Get Reviews (paginated) ──────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviews(Long photographerId, int page, int size) {
        findApprovedPhotographer(photographerId); // validate exists
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository
                .findByPhotographerIdAndDeletedFalse(photographerId, pageable)
                .map(this::toResponse);
    }

    // ─── Rating Summary ───────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PhotographerRatingSummary getRatingSummary(Long photographerId) {
        findApprovedPhotographer(photographerId); // validate exists

        Double avg = reviewRepository.findAverageRatingByPhotographerId(photographerId);
        long total = reviewRepository.countByPhotographerIdAndDeletedFalse(photographerId);

        return PhotographerRatingSummary.builder()
                .photographerId(photographerId)
                .averageRating(avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0) // round to 1 decimal
                .totalReviews(total)
                .build();
    }

    // ─── Private Helpers ──────────────────────────────────────────────────

    private User findApprovedPhotographer(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Photographer not found"));

        if (user.getRole() != Role.PHOTOGRAPHER) {
            throw new RuntimeException("User is not a photographer");
        }

        if (user.getVerificationStatus() != VerificationStatus.APPROVED) {
            throw new RuntimeException("Photographer is not verified");
        }

        return user;
    }

    private User findUser(Long id) {
        return userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private PhotographerReview findActiveReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .filter(r -> !r.isDeleted())
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    // ─── Mapper ───────────────────────────────────────────────────────────

    private ReviewResponse toResponse(PhotographerReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .edited(review.getUpdatedAt() != null && !review.getUpdatedAt().equals(review.getCreatedAt()))
                .reviewerId(review.getReviewer().getId())
                .reviewerUsername(review.getReviewer().getActualUsername())
                .reviewerProfilePicture(review.getReviewer().getProfilePicture())
                .photographerId(review.getPhotographer().getId())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
