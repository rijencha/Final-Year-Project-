package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.friends.FriendshipResponseDto;
import com.example.photoGroupe.model.Friendship;
import com.example.photoGroupe.model.FriendshipStatus;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.FriendshipRepository;
import com.example.photoGroupe.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendshipServiceImpl implements FriendshipService{

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;


    @Override
    public FriendshipResponseDto sendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId))
            throw new IllegalArgumentException("Cannot send a friend request to yourself");

        friendshipRepo.findBetween(senderId, receiverId).ifPresent(f -> {
            switch (f.getStatus()) {
                case PENDING  -> throw new IllegalStateException("Friend request already sent");
                case ACCEPTED -> throw new IllegalStateException("You are already friends");
                case BLOCKED  -> throw new IllegalStateException("Action not allowed");
                case CANCELLED -> { /* allow re-sending after cancel */ }
            }
        });

        // If a CANCELLED record exists, reuse it instead of creating a duplicate
        Optional<Friendship> existing = friendshipRepo.findBetween(senderId, receiverId);
        Friendship f;
        if (existing.isPresent() && existing.get().getStatus() == FriendshipStatus.CANCELLED) {
            f = existing.get();
            f.setSender(userRepo.findById(senderId).orElseThrow());
            f.setReceiver(userRepo.findById(receiverId).orElseThrow());
            f.setStatus(FriendshipStatus.PENDING);
        } else {
            User sender   = userRepo.findById(senderId).orElseThrow();
            User receiver = userRepo.findById(receiverId).orElseThrow();
            f = new Friendship(sender, receiver);
        }

        return toDto(friendshipRepo.save(f), senderId);
    }

    @Override
    public FriendshipResponseDto acceptRequest(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!f.getReceiver().getId().equals(currentUserId))
            throw new AccessDeniedException("Only the receiver can accept");
        if (f.getStatus() != FriendshipStatus.PENDING)
            throw new IllegalStateException("Request is not in PENDING state");

        f.setStatus(FriendshipStatus.ACCEPTED);
        return toDto(friendshipRepo.save(f), currentUserId);
    }

    @Override
    public void declineRequest(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!f.getReceiver().getId().equals(currentUserId))
            throw new AccessDeniedException("Only the receiver can decline");
        if (f.getStatus() != FriendshipStatus.PENDING)
            throw new IllegalStateException("Request is not pending");

        f.setStatus(FriendshipStatus.CANCELLED);
        friendshipRepo.save(f);
    }

    @Override
    public void cancelRequest(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();

        if (!f.getSender().getId().equals(currentUserId))
            throw new AccessDeniedException("Only the sender can cancel");
        if (f.getStatus() != FriendshipStatus.PENDING)
            throw new IllegalStateException("Request is not pending");

        f.setStatus(FriendshipStatus.CANCELLED);
        friendshipRepo.save(f);
    }

    // ─── Friend Management ────────────────────────────────────────────────

    @Override
    public void unfriend(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();
        assertInvolved(f, currentUserId);

        if (f.getStatus() != FriendshipStatus.ACCEPTED)
            throw new IllegalStateException("You are not friends");

        f.setStatus(FriendshipStatus.CANCELLED);
        friendshipRepo.save(f);
    }

    @Override
    public void blockUser(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();
        assertInvolved(f, currentUserId);
        f.setStatus(FriendshipStatus.BLOCKED);
        friendshipRepo.save(f);
    }

    @Override
    public void unblockUser(Long friendshipId, Long currentUserId) {
        Friendship f = friendshipRepo.findById(friendshipId).orElseThrow();
        assertInvolved(f, currentUserId);

        if (f.getStatus() != FriendshipStatus.BLOCKED)
            throw new IllegalStateException("User is not blocked");

        f.setStatus(FriendshipStatus.CANCELLED); // reset to neutral
        friendshipRepo.save(f);
    }

// ─── Queries ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getFriends(Long userId) {
        return friendshipRepo.findAllAcceptedFriends(userId)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getPendingRequests(Long userId) {
        return friendshipRepo.findByReceiverIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getSentRequests(Long userId) {
        return friendshipRepo.findBySenderIdAndStatus(userId, FriendshipStatus.PENDING)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    public List<FriendshipResponseDto> getBlockedUsers(Long userId) {
        return friendshipRepo.findBlockedByUser(userId)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FriendshipResponseDto> getMutualFriends(Long userId, Long otherUserId) {
        return friendshipRepo.findMutualFriends(userId, otherUserId)
                .stream()
                .map(f -> toDto(f, userId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getFriendCount(Long userId) {
        return friendshipRepo.countAcceptedFriends(userId);
    }

    // ─── Status Checks ────────────────────────────────────────────────────

    @Override
    public void assertFriends(Long a, Long b) {
        if (!friendshipRepo.areFriends(a, b))
            throw new AccessDeniedException("Users are not friends");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean areFriends(Long a, Long b) {
        return friendshipRepo.areFriends(a, b);
    }

    @Override
    @Transactional(readOnly = true)
    public String getFriendshipStatus(Long currentUserId, Long otherUserId) {
        return friendshipRepo.findBetween(currentUserId, otherUserId)
                .map(f -> switch (f.getStatus()) {
                    case ACCEPTED  -> "FRIENDS";
                    case BLOCKED   -> "BLOCKED";
                    case PENDING   -> f.getSender().getId().equals(currentUserId)
                            ? "PENDING_SENT"
                            : "PENDING_RECEIVED";
                    case CANCELLED -> "NONE";
                })
                .orElse("NONE");
    }

    // ─── Private Helpers ──────────────────────────────────────────────────

    private void assertInvolved(Friendship f, Long userId) {
        boolean involved = f.getSender().getId().equals(userId)
                || f.getReceiver().getId().equals(userId);
        if (!involved) throw new AccessDeniedException("Not your friendship");
    }

    private FriendshipResponseDto toDto(Friendship f, Long currentUserId) {
        boolean isSender = f.getSender().getId().equals(currentUserId);
        User friend = isSender ? f.getReceiver() : f.getSender();
        return new FriendshipResponseDto(
                f.getId(),
                friend.getId(),
                friend.getActualUsername(),
                friend.getFullName(),
                friend.getProfilePicture(),
                f.getStatus(),
                isSender,
                f.getCreatedAt()
        );
    }
}
