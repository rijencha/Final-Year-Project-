package com.example.photoGroupe.repo.restrict;

import com.example.photoGroupe.model.restrict.FeedExclusion;
import com.example.photoGroupe.model.restrict.FeedExclusionScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedExclusionRepository extends JpaRepository<FeedExclusion, Long> {

    List<FeedExclusion> findAllByOwnerId(Long ownerId);

    Optional<FeedExclusion> findByOwnerIdAndScopeAndPinId(Long ownerId, FeedExclusionScope scope, Long pinId);

    Optional<FeedExclusion> findByOwnerIdAndScopeAndExcludedUserId(Long ownerId, FeedExclusionScope scope, Long excludedUserId);

    Optional<FeedExclusion> findByOwnerIdAndScopeAndCategoryId(Long ownerId, FeedExclusionScope scope, Long categoryId);

    void deleteById(Long id);

    // ── Fast lookups for feed filtering ──────────────────────────────────

    @Query("select f.pin.id from FeedExclusion f where f.owner.id = :ownerId and f.scope = 'PIN'")
    List<Long> findExcludedPinIds(@Param("ownerId") Long ownerId);

    @Query("select f.excludedUser.id from FeedExclusion f where f.owner.id = :ownerId and f.scope = 'USER'")
    List<Long> findExcludedUserIds(@Param("ownerId") Long ownerId);

    @Query("select f.category.id from FeedExclusion f where f.owner.id = :ownerId and f.scope = 'CATEGORY'")
    List<Long> findExcludedCategoryIds(@Param("ownerId") Long ownerId);
}