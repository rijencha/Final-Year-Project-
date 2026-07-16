package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Pin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PinRepository extends JpaRepository<Pin,Long> {
    // Feed: all non-deleted pins, newest first (for home/explore)
    Page<Pin> findByDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(Pageable pageable);

    // Profile: pins by a specific user
    Page<Pin> findByUserIdAndDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Tag search: pins containing a tag keyword
    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND LOWER(p.tags) LIKE LOWER(CONCAT('%', :tag, '%')) ORDER BY p.createdAt DESC")
    Page<Pin> findByTag(@Param("tag") String tag, Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.category.id = :categoryId AND p.deleted = false AND p.albumOnly = false ORDER BY p.createdAt DESC")
    Page<Pin> findByCategoryIdAndDeletedFalseAndAlbumOnlyFalse(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Pin p WHERE p.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // PinRepository.java
    @Query(
            value = "SELECT * FROM pins WHERE is_deleted = false AND is_suspended = false ORDER BY RAND()",
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false",
            nativeQuery = true
    )
    Page<Pin> findAllShuffled(Pageable pageable);

    @Query("""
        select p from Pin p
        join fetch p.user
        left join fetch p.category
        where p.deleted = false and p.albumOnly = false
        order by p.createdAt desc
    """)
    Page<Pin> findFeedWithUserAndCategory(Pageable pageable);

    @Query(
            value = """
    SELECT * FROM pins
    WHERE is_deleted = false AND is_suspended = false AND album_only = false
    AND category_id IN (
        SELECT id FROM categories WHERE name IN (:interests)
    )
    ORDER BY RAND()
    """,
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false AND album_only = false",
            nativeQuery = true
    )
    List<Pin> findByInterestsRaw(@Param("interests") List<String> interests, Pageable pageable);

    @Query(
            value = """
    SELECT * FROM pins
    WHERE is_deleted = false AND is_suspended = false AND album_only = false
    AND (category_id IS NULL OR category_id NOT IN (
        SELECT id FROM categories WHERE name IN (:interests)
    ))
    ORDER BY RAND()
    """,
            countQuery = "SELECT COUNT(*) FROM pins WHERE is_deleted = false AND is_suspended = false AND album_only = false",
            nativeQuery = true
    )
    List<Pin> findExcludingInterestsRaw(@Param("interests") List<String> interests, Pageable pageable);

    List<Pin> findByDeletedFalseAndSuspendedFalseAndAlbumOnlyFalse();

    // Related by same category (excluding current pin)
    @Query("SELECT p FROM Pin p WHERE p.category.id = :categoryId AND p.id != :pinId AND p.deleted = false AND p.suspended = false ORDER BY p.createdAt DESC")
    Page<Pin> findRelatedByCategory(@Param("categoryId") Long categoryId, @Param("pinId") Long pinId, Pageable pageable);

    @Query(
            value = """
        SELECT * FROM pins
        WHERE id != :pinId
        AND is_deleted = false
        AND is_suspended = false
        AND (:tags IS NULL OR FIND_IN_SET(TRIM(:tags), REPLACE(tags, ', ', ',')) > 0
             OR tags REGEXP :pattern)
        ORDER BY created_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM pins WHERE id != :pinId AND is_deleted = false",
            nativeQuery = true
    )
    Page<Pin> findRelatedByTagsNative(@Param("pinId") Long pinId,
                                      @Param("pattern") String pattern,
                                      Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND p.suspended = false AND p.albumOnly = false " +
            "ORDER BY (SELECT COUNT(s) FROM SavedPin s WHERE s.pin = p) DESC")
    List<Pin> findMostSaved(Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND p.suspended = false AND p.albumOnly = false " +
            "ORDER BY (SELECT COUNT(sh) FROM PinShare sh WHERE sh.pin = p) DESC")
    List<Pin> findMostShared(Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND p.suspended = false AND p.albumOnly = false " +
            "ORDER BY (SELECT COUNT(d) FROM PinDownload d WHERE d.pin = p) DESC")
    List<Pin> findMostDownloaded(Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.deleted = false AND p.suspended = false AND p.albumOnly = false " +
            "ORDER BY p.viewCount DESC")
    List<Pin> findMostViewed(Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.user.id = :userId AND p.deleted = false AND p.suspended = false " +
            "ORDER BY (SELECT COUNT(s) FROM SavedPin s WHERE s.pin = p) DESC")
    List<Pin> findMostSavedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.user.id = :userId AND p.deleted = false AND p.suspended = false " +
            "ORDER BY (SELECT COUNT(sh) FROM PinShare sh WHERE sh.pin = p) DESC")
    List<Pin> findMostSharedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.user.id = :userId AND p.deleted = false AND p.suspended = false " +
            "ORDER BY (SELECT COUNT(d) FROM PinDownload d WHERE d.pin = p) DESC")
    List<Pin> findMostDownloadedByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Pin p WHERE p.user.id = :userId AND p.deleted = false AND p.suspended = false " +
            "ORDER BY p.viewCount DESC")
    List<Pin> findMostViewedByUser(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Pin p SET p.viewCount = p.viewCount + 1 WHERE p.id = :pinId AND p.deleted = false AND p.suspended = false")
    int incrementViewCount(@Param("pinId") Long pinId);

    @Query("""
    select p from Pin p
        where p.deleted = false and p.suspended = false and p.albumOnly = false
          and (
            lower(p.title) like lower(concat('%', :q, '%'))
            or lower(p.description) like lower(concat('%', :q, '%'))
            or lower(p.tags) like lower(concat('%', :q, '%'))
          )
    """)
    Page<Pin> search(@Param("q") String q, Pageable pageable);
}
