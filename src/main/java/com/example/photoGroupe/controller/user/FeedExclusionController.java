package com.example.photoGroupe.controller.user;

import com.example.photoGroupe.dto.restrict.FeedExclusionDtos.FeedExclusionRequest;
import com.example.photoGroupe.dto.restrict.FeedExclusionDtos.FeedExclusionResponse;
import com.example.photoGroupe.model.restrict.FeedExclusionScope;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.restrict.FeedExclusionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/feed-exclusions")
@RequiredArgsConstructor
public class FeedExclusionController {

    private final FeedExclusionService exclusionService;

    /** Mark a pin / user's pins / category as "not interested". Silent. */
    @PostMapping
    public ResponseEntity<Void> exclude(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody FeedExclusionRequest request
    ) {
        exclusionService.exclude(currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }

    /** Undo a "not interested" mark. */
    @DeleteMapping
    public ResponseEntity<Void> undo(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam FeedExclusionScope scope,
            @RequestParam Long targetId
    ) {
        exclusionService.undoExclusion(currentUser.getId(), scope, targetId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<List<FeedExclusionResponse>> myExclusions(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(exclusionService.getMyExclusions(currentUser.getId()));
    }
}