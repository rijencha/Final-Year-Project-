package com.example.photoGroupe.controller.pin;
import com.example.photoGroupe.dto.category.CategoryPreferenceDtos.*;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.category.CategoryPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/users/category-preferences")
@RequiredArgsConstructor
public class CategoryPreferenceController {

    private final CategoryPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<List<CategoryPreferenceResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(preferenceService.getAllWithPreferences(currentUser.getId()));
    }

    @PostMapping("/{categoryId}/see-less")
    public ResponseEntity<Void> seeLess(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        preferenceService.seeLess(currentUser.getId(), categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{categoryId}/see-more")
    public ResponseEntity<Void> seeMore(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        preferenceService.seeMore(currentUser.getId(), categoryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{categoryId}/interest")
    public ResponseEntity<Void> addInterest(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        preferenceService.addInterest(currentUser.getId(), categoryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> reset(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        preferenceService.resetToDefault(currentUser.getId(), categoryId);
        return ResponseEntity.noContent().build();
    }
}
