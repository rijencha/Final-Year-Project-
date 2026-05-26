package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.follow.FollowStatsDTO;
import com.example.photoGroupe.dto.follow.FollowUserDTO;
import com.example.photoGroupe.model.Follow;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.BlockRepository;
import com.example.photoGroupe.repo.FollowRepository;
import com.example.photoGroupe.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository   userRepository;
    private final BlockRepository  blockRepository;

    public FollowServiceImpl(FollowRepository followRepository,
                             UserRepository userRepository,
                             BlockRepository blockRepository) {
        this.followRepository = followRepository;
        this.userRepository   = userRepository;
        this.blockRepository  = blockRepository;
    }

    // ─── follow ───────────────────────────────────────────────────────────

    @Override
    public FollowStatsDTO follow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot follow yourself.");
        }

        User follower  = findUserOrThrow(currentUserId);
        User following = findUserOrThrow(targetUserId);

        // Block check — same error regardless of direction to avoid leaking info
        if (blockRepository.existsBlockBetween(currentUserId, targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Follow not allowed.");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "You are already following this user.");
        }

        followRepository.save(new Follow(follower, following));
        return buildStats(following, follower);
    }

    // ─── unfollow ─────────────────────────────────────────────────────────

    @Override
    public FollowStatsDTO unfollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot unfollow yourself.");
        }

        User follower  = findUserOrThrow(currentUserId);
        User following = findUserOrThrow(targetUserId);

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "You are not following this user.");
        }

        followRepository.deleteByFollowerAndFollowing(follower, following);
        return buildStats(following, follower);
    }

    // ─── getFollowers ─────────────────────────────────────────────────────

    @Override
    public Page<FollowUserDTO> getFollowers(Long userId, Pageable pageable) {
        User user = findUserOrThrow(userId);
        return followRepository
                .findByFollowing(user, pageable)
                .map(f -> toFollowUserDTO(f.getFollower(), f));
    }

    // ─── getFollowing ─────────────────────────────────────────────────────

    @Override
    public Page<FollowUserDTO> getFollowing(Long userId, Pageable pageable) {
        User user = findUserOrThrow(userId);
        return followRepository
                .findByFollower(user, pageable)
                .map(f -> toFollowUserDTO(f.getFollowing(), f));
    }

    // ─── getFollowStats ───────────────────────────────────────────────────

    @Override
    public FollowStatsDTO getFollowStats(Long targetUserId, Long currentUserId) {
        User target  = findUserOrThrow(targetUserId);
        User current = findUserOrThrow(currentUserId);
        return buildStats(target, current);
    }

    // ─── isFollowing ──────────────────────────────────────────────────────

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        User follower  = findUserOrThrow(followerId);
        User following = findUserOrThrow(followingId);
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    // ─── private helpers ──────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + id));
    }

    private FollowStatsDTO buildStats(User target, User currentUser) {
        long followersCount      = followRepository.countByFollowing(target);
        long followingCount      = followRepository.countByFollower(target);
        boolean isFollowingThem  = followRepository.existsByFollowerAndFollowing(currentUser, target);
        boolean isFollowingMe    = followRepository.existsByFollowerAndFollowing(target, currentUser);
        boolean isBlockingThem   = blockRepository.existsByBlockerAndBlocked(currentUser, target);
        boolean isBlockedByThem  = blockRepository.existsByBlockerAndBlocked(target, currentUser);

        return new FollowStatsDTO(
                target.getId(),
                target.getActualUsername(),
                followersCount,
                followingCount,
                isFollowingThem,
                isFollowingMe,
                isBlockingThem,
                isBlockedByThem
        );
    }

    private FollowUserDTO toFollowUserDTO(User user, Follow follow) {
        return new FollowUserDTO(
                user.getId(),
                user.getActualUsername(),
                user.getFullName(),
                user.getProfilePicture(),
                user.isVerified(),
                follow.getCreatedAt()
        );
    }
}