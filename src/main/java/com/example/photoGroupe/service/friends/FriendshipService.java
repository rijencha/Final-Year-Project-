package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.friends.FriendshipResponseDto;


import java.util.List;

public interface FriendshipService {

    // ─── Request Management ───────────────────────────────────────────────
    FriendshipResponseDto sendRequest(Long senderId, Long receiverId);
    FriendshipResponseDto acceptRequest(Long friendshipId, Long currentUserId);
    void declineRequest(Long friendshipId, Long currentUserId);   // receiver declines
    void cancelRequest(Long friendshipId, Long currentUserId);    // sender cancels before accept

    // ─── Friend Management ────────────────────────────────────────────────
    void unfriend(Long friendshipId, Long currentUserId);
    void blockUser(Long friendshipId, Long currentUserId);
    void unblockUser(Long friendshipId, Long currentUserId);

    // ─── Queries ──────────────────────────────────────────────────────────
    List<FriendshipResponseDto> getFriends(Long userId);
    List<FriendshipResponseDto> getPendingRequests(Long userId);       // incoming
    List<FriendshipResponseDto> getSentRequests(Long userId);          // outgoing
    List<FriendshipResponseDto> getBlockedUsers(Long userId);
    List<FriendshipResponseDto> getMutualFriends(Long userId, Long otherUserId);
    long getFriendCount(Long userId);

    // ─── Status Checks ────────────────────────────────────────────────────
    void assertFriends(Long a, Long b);
    boolean areFriends(Long a, Long b);
    String getFriendshipStatus(Long currentUserId, Long otherUserId); // NONE/PENDING_SENT/PENDING_RECEIVED/FRIENDS/BLOCKED
}
