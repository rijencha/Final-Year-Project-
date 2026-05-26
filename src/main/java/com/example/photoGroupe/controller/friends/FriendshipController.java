package com.example.photoGroupe.controller.friends;

import com.example.photoGroupe.dto.friends.FriendshipRequestDto;
import com.example.photoGroupe.dto.friends.FriendshipResponseDto;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.friends.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/friends")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    // ─── Requests ─────────────────────────────────────────────────────────

    @PostMapping("/request")
    public ResponseEntity<FriendshipResponseDto> send(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestBody FriendshipRequestDto dto) {
        return ResponseEntity.status(201).body(friendshipService.sendRequest(me.getId(), dto.receiverId()));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<FriendshipResponseDto> accept(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        return ResponseEntity.ok(friendshipService.acceptRequest(id, me.getId()));
    }

    @PutMapping("/{id}/decline")
    public ResponseEntity<Void> decline(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        friendshipService.declineRequest(id, me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        friendshipService.cancelRequest(id, me.getId());
        return ResponseEntity.noContent().build();
    }


    // ─── Friend Management ────────────────────────────────────────────────

    @DeleteMapping("/{id}/unfriend")
    public ResponseEntity<Void> unfriend(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        friendshipService.unfriend(id, me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<Void> block(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        friendshipService.blockUser(id, me.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<Void> unblock(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long id) {
        friendshipService.unblockUser(id, me.getId());
        return ResponseEntity.noContent().build();
    }

    // ─── Queries ──────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<FriendshipResponseDto>> myFriends(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(friendshipService.getFriends(me.getId()));
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendshipResponseDto>> incoming(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(friendshipService.getPendingRequests(me.getId()));
    }

    @GetMapping("/requests/sent")
    public ResponseEntity<List<FriendshipResponseDto>> sent(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(friendshipService.getSentRequests(me.getId()));
    }

    @GetMapping("/blocked")
    public ResponseEntity<List<FriendshipResponseDto>> blocked(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(friendshipService.getBlockedUsers(me.getId()));
    }

    @GetMapping("/mutual/{otherUserId}")
    public ResponseEntity<List<FriendshipResponseDto>> mutual(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long otherUserId) {
        return ResponseEntity.ok(friendshipService.getMutualFriends(me.getId(), otherUserId));
    }

    @GetMapping("/status/{otherUserId}")
    public ResponseEntity<Map<String, String>> status(
            @AuthenticationPrincipal CustomUserDetails me, @PathVariable Long otherUserId) {
        return ResponseEntity.ok(Map.of("status",
                friendshipService.getFriendshipStatus(me.getId(), otherUserId)));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(Map.of("count", friendshipService.getFriendCount(me.getId())));
    }
}
