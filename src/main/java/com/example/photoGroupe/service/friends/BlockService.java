package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.follow.BlockUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlockService {

    /** Block targetUserId. Silently removes any follow relationship in both directions. */
    void block(Long currentUserId, Long targetUserId);

    /** Unblock targetUserId. Does NOT restore the follow — user must re-follow manually. */
    void unblock(Long currentUserId, Long targetUserId);

    /** Remove a follower (currentUser removes followerUserId from their followers list). */
    void removeFollower(Long currentUserId, Long followerUserId);

    /** Paginated list of users blocked by currentUserId. */
    Page<BlockUserDTO> getBlockedUsers(Long currentUserId, Pageable pageable);

    /** Quick check — has currentUserId blocked targetUserId? */
    boolean isBlocking(Long currentUserId, Long targetUserId);
}