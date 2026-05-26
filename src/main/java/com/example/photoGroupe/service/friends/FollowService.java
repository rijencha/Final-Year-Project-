package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.follow.FollowStatsDTO;
import com.example.photoGroupe.dto.follow.FollowUserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FollowService {
    FollowStatsDTO follow(Long currentUserId, Long targetUserId);

    FollowStatsDTO unfollow(Long currentUserId, Long targetUserId);

    Page<FollowUserDTO> getFollowers(Long userId, Pageable pageable);

    Page<FollowUserDTO> getFollowing(Long userId, Pageable pageable);

    FollowStatsDTO getFollowStats(Long targetUserId, Long currentUserId);

    boolean isFollowing(Long followerId, Long followingId);
}