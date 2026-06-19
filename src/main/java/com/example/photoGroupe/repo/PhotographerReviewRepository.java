package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.rating.PhotographerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhotographerReviewRepository extends JpaRepository<PhotographerReview, Long> {
    // All non-deleted reviews for a photographer (paginated)
    Page<PhotographerReview> findByPhotographerIdAndDeletedFalse(Long photographerId, Pageable pageable);

    // Check if reviewer already left a review for this photographer
    boolean existsByReviewerIdAndPhotographerIdAndDeletedFalse(Long reviewerId, Long photographerId);

    // Average rating for a photographer
    @Query("SELECT AVG(r.rating) FROM PhotographerReview r WHERE r.photographer.id = :photographerId AND r.deleted = false")
    Double findAverageRatingByPhotographerId(@Param("photographerId") Long photographerId);

    // Total review count
    long countByPhotographerIdAndDeletedFalse(Long photographerId);

    List<PhotographerReview> findByPhotographerIdAndDeletedFalseOrderByCreatedAtDesc(Long photographerId);

    Optional<PhotographerReview> findByReviewerIdAndPhotographerId(Long reviewerId, Long photographerId);

    @Query("""
    SELECT r.photographer.id,
           AVG(r.rating)   AS avgRating,
           COUNT(r.id)     AS reviewCount
    FROM PhotographerReview r
    WHERE r.deleted = false
      AND r.rating > 0
    GROUP BY r.photographer.id
    ORDER BY avgRating DESC, reviewCount DESC
    """)
    List<Object[]> findTopPhotographerStats(Pageable pageable);
}
