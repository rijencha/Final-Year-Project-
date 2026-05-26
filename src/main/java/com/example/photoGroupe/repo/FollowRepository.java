package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Follow;
import com.example.photoGroupe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    /** Exact follow relationship lookup — used for follow/unfollow checks */
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    /** Does this follow relationship already exist? */
    boolean existsByFollowerAndFollowing(User follower, User following);

    // ─── Followers (people who follow a given user) ───────────────────────

    /** All users following `following`, paginated */
    Page<Follow> findByFollowing(User following, Pageable pageable);

    /** Count how many followers a user has */
    long countByFollowing(User following);

    // ─── Following (people a given user follows) ──────────────────────────

    /** All users that `follower` is following, paginated */
    Page<Follow> findByFollower(User follower, Pageable pageable);

    /** Count how many accounts a user follows */
    long countByFollower(User follower);

    // ─── Mutual / suggestion helpers ──────────────────────────────────────

    /**
     * Returns users that BOTH userA and userB follow (mutual following).
     * Useful for "people you may know" suggestions.
     */
    @Query("""
        SELECT f1.following FROM Follow f1
        WHERE f1.follower.id = :userAId
          AND f1.following.id IN (
              SELECT f2.following.id FROM Follow f2
              WHERE f2.follower.id = :userBId
          )
        """)
    Page<User> findMutualFollowing(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId,
            Pageable pageable
    );

    /** Remove a follow row by the two user objects (for unfollow) */
    void deleteByFollowerAndFollowing(User follower, User following);
}