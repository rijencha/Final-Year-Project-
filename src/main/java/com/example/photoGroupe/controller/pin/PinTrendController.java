package com.example.photoGroupe.controller.pin;

import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.upload.PinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/pins")
@RequiredArgsConstructor
public class PinTrendController {

    private final PinsService pinsService;

    @GetMapping("/trending/saved")
    public ResponseEntity<List<PinResponse>> mostSaved(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getMostSavedPins(limit, uid));
    }

    @GetMapping("/trending/shared")
    public ResponseEntity<List<PinResponse>> mostShared(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getMostSharedPins(limit, uid));
    }

    @GetMapping("/trending/downloaded")
    public ResponseEntity<List<PinResponse>> mostDownloaded(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getMostDownloadedPins(limit, uid));
    }

    @GetMapping("/trending/viewed")
    public ResponseEntity<List<PinResponse>> mostViewed(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getMostViewedPins(limit, uid));
    }

    @GetMapping("/users/{userId}/most-saved")
    public ResponseEntity<List<PinResponse>> userMostSaved(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getUserMostSavedPins(userId, limit, uid));
    }

    @GetMapping("/users/{userId}/most-shared")
    public ResponseEntity<List<PinResponse>> userMostShared(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getUserMostSharedPins(userId, limit, uid));
    }

    @GetMapping("/users/{userId}/most-downloaded")
    public ResponseEntity<List<PinResponse>> userMostDownloaded(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getUserMostDownloadedPins(userId, limit, uid));
    }

    @GetMapping("/users/{userId}/most-viewed")
    public ResponseEntity<List<PinResponse>> userMostViewed(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails user) {
        Long uid = user != null ? user.getId() : null;
        return ResponseEntity.ok(pinsService.getUserMostViewedPins(userId, limit, uid));
    }
}
