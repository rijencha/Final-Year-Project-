package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin,Long> {
    // Feed: all non-deleted pins, newest first (for home/explore)
    Page<Pin> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    // Profile: pins by a specific user
    Page<Pin> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Tag search: pins containing a tag keyword
    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY p.createdAt DESC")
    Page<Pin> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.category.id = :categoryId AND p.deleted = false ORDER BY p.createdAt DESC")
    Page<Pin> findByCategoryIdAndDeletedFalse(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pin p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // PinRepository.java
    @Query(
            value = "SELECT * FROM pins WHERE is_deleted = false AND is_suspended = false ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false",
            nativeQuery = true
    )
    Page<Pin> findAllShuffled(Pageable pageable);

    @Query(
            value = """
        SELECT * FROM pins
        WHERE is_deleted = false AND is_suspended = false
        AND category_id IN (
            SELECT id FROM categories WHERE name IN (:interests)
        )
        ORDER BY RAND()
        """,
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false",
            nativeQuery = true
    )
    List<Pin> findByInterestsRaw(@Param("interests") List<String> interests, Pageable pageable);

    @Query(
            value = """
        SELECT * FROM pins
        WHERE is_deleted = false AND is_suspended = false
        AND (category_id IS NULL OR category_id NOT IN (
            SELECT id FROM categories WHERE name IN (:interests)
        ))
        ORDER BY RAND()
        """,
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false",
            nativeQuery = true
    )
    List<Pin> findExcludingInterestsRaw(@Param("interests") List<String> interests, Pageable pageable);

    List<Pin> findByDeletedFalseAndSuspendedFalse();

    // Related by same category (excluding current pin)
    @Query("SELECT p FROM Pin p WHERE p.category.id = :categoryId AND p.id != :pinId AND p.deleted = false AND p.suspended = false ORDER BY p.createdAt DESC")
    Page<Pin> findRelatedByCategory(@Param("categoryId") Long categoryId, @Param("pinId") Long pinId, Pageable pageable);

    // Related by tags (at least one matching tag)
    @Query("SELECT DISTINCT p FROM Pin p JOIN p.tags t WHERE t IN :tags AND p.id != :pinId AND p.deleted = false AND p.suspended = false ORDER BY p.createdAt DESC")
    Page<Pin> findRelatedByTags(@Param("tags") List<String> tags, @Param("pinId") Long pinId, Pageable pageable);
}
