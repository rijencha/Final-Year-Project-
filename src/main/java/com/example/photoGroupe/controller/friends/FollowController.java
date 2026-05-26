package com.example.photoGroupe.controller.friends;

import com.example.photoGroupe.dto.follow.FollowStatsDTO;
import com.example.photoGroupe.dto.follow.FollowUserDTO;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.friends.FollowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{targetId}/follow")
    public ResponseEntity<FollowStatsDTO> follow(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        FollowStatsDTO stats = followService.follow(currentUser.getId(), targetId);
        return ResponseEntity.ok(stats);
    }

    // ─── DELETE /api/v1/users/{targetId}/follow ───────────────────────────
    /**
     * Authenticated user unfollows `targetId`.
     * Returns updated follow stats for the target profile.
     */
    @DeleteMapping("/{targetId}/follow")
    public ResponseEntity<FollowStatsDTO> unfollow(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        FollowStatsDTO stats = followService.unfollow(currentUser.getId(), targetId);
        return ResponseEntity.ok(stats);
    }

    // ─── GET /api/v1/users/{userId}/followers ─────────────────────────────
    /**
     * Returns a paginated list of users who follow `userId`.
     * Default page size: 20, sorted by followedAt desc.
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<Page<FollowUserDTO>> getFollowers(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(followService.getFollowers(userId, pageable));
    }

    // ─── GET /api/v1/users/{userId}/following ─────────────────────────────
    /**
     * Returns a paginated list of users that `userId` follows.
     * Default page size: 20, sorted by followedAt desc.
     */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Page<FollowUserDTO>> getFollowing(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(followService.getFollowing(userId, pageable));
    }

    // ─── GET /api/v1/users/{userId}/follow-stats ──────────────────────────
    /**
     * Returns follower/following counts for `userId` plus whether the
     * currently authenticated user already follows them.
     */
    @GetMapping("/{userId}/follow-stats")
    public ResponseEntity<FollowStatsDTO> getFollowStats(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        FollowStatsDTO stats = followService.getFollowStats(userId, currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    // ─── GET /api/v1/users/{userId}/is-following ──────────────────────────
    /**
     * Quick boolean check — does the current user follow `userId`?
     * Useful for toggling a Follow/Unfollow button in the UI.
     */
    @GetMapping("/{userId}/is-following")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean result = followService.isFollowing(currentUser.getId(), userId);
        return ResponseEntity.ok(result);
    }
}
