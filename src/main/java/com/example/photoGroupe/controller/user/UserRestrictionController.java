package com.example.photoGroupe.controller.user;


import com.example.photoGroupe.dto.restrict.RestrictionDtos;
import com.example.photoGroupe.dto.restrict.RestrictionDtos.*;
import com.example.photoGroupe.model.restrict.RestrictionType;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.restrict.UserRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/restriction")
@RequiredArgsConstructor
public class UserRestrictionController {

    private final UserRestrictionService restrictionService;

    /** Apply a restriction. Silent — the target user is never notified. */
    @PostMapping
    public ResponseEntity<Void> restrict(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestBody RestrictionDtos.RestrictionRequest request
    ) {
        restrictionService.restrict(currentUser.getId(), request.getTargetUserId(), request.getType());
        return ResponseEntity.noContent().build();
    }

    /** Lift a restriction. */
    @DeleteMapping
    public ResponseEntity<Void> unrestrict(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @RequestParam Long targetUserId,
            @RequestParam RestrictionType type
    ) {
        restrictionService.unrestrict(currentUser.getId(), targetUserId, type);
        return ResponseEntity.noContent().build();
    }

    /** The current user's own list of restrictions they've placed on others. */
    @GetMapping("/mine")
    public ResponseEntity<List<RestrictionResponse>> myRestrictions(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(restrictionService.getMyRestrictions(currentUser.getId()));
    }

    /**
     * Which restriction types the current user has on one specific person.
     * Useful for rendering the correct toggle states on that person's profile.
     */
    @GetMapping("/on/{targetUserId}")
    public ResponseEntity<Map<String, Object>> restrictionsOn(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable Long targetUserId
    ) {
        List<RestrictionType> types = restrictionService.getRestrictionTypesOn(currentUser.getId(), targetUserId);
        return ResponseEntity.ok(Map.of("targetUserId", targetUserId, "restrictedTypes", types));
    }
}
