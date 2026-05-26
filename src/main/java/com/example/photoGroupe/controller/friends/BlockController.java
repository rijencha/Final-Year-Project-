package com.example.photoGroupe.controller.friends;


import com.example.photoGroupe.dto.follow.BlockUserDTO;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.friends.BlockService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    /** POST /api/users/{targetId}/block */
    @PostMapping("/{targetId}/block")
    public ResponseEntity<Void> block(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails me) {
        blockService.block(me.getId(), targetId);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/users/{targetId}/block */
    @DeleteMapping("/{targetId}/block")
    public ResponseEntity<Void> unblock(
            @PathVariable Long targetId,
            @AuthenticationPrincipal CustomUserDetails me) {
        blockService.unblock(me.getId(), targetId);
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/users/followers/{followerId} */
    @DeleteMapping("/followers/{followerId}")
    public ResponseEntity<Void> removeFollower(
            @PathVariable Long followerId,
            @AuthenticationPrincipal CustomUserDetails me) {
        blockService.removeFollower(me.getId(), followerId);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/users/blocked?page=0&size=20 */
    @GetMapping("/blocked")
    public ResponseEntity<Page<BlockUserDTO>> getBlocked(
            Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails me) {
        return ResponseEntity.ok(blockService.getBlockedUsers(me.getId(), pageable));
    }
}