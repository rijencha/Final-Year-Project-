package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
