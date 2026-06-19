package com.example.photoGroupe.controller.user;

import com.example.photoGroupe.dto.detail.UpdateUserRequest;
import com.example.photoGroupe.dto.detail.UpgradeToPhotographerRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.report.CreateReportRequest;
import com.example.photoGroupe.dto.report.ReportResponse;
import com.example.photoGroupe.exception.UserNotFoundException;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.photographer.PhotographerProfileService;
import com.example.photoGroupe.service.report.ReportService;
import com.example.photoGroupe.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ReportService  reportService;
    private final PhotographerProfileService service;


    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummary> getPublicUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPublicUserById(id));
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummary> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        User currentUser = userRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow(() -> new RuntimeException("User not found"));
        if(userDetails == null) {
            throw new UserNotFoundException("User not found");
        }

        return ResponseEntity.ok(userService.updateUser(id, request, userDetails.getUser()));
    }

    // Browse all verified photographers
    @GetMapping("/photographers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PhotographerDetail>> getAllPhotographers() {
        return ResponseEntity.ok(userService.getAllPhotographers());
    }

    @GetMapping("/photographers/top")
    public ResponseEntity<List<PhotographerDetail>> getTopPhotographers(
            @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(userService.getTopPhotographers(limit));
    }

    // Full public profile of a specific photographer
    @GetMapping("/photographers/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotographerDetail> getPhotographerDetail(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPhotographerDetail(id));
    }

    @PutMapping("/upgrade-to-photographer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummary> upgradeToPhotographer(
            @RequestBody @Valid UpgradeToPhotographerRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                userService.upgradeToPhotographer(userDetails.getId(), request)
        );
    }

    @PatchMapping(value = "/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
//            User user = userRepository.findByEmail(userDetails.getUsername())
//                    .orElseThrow(() -> new RuntimeException("User not found"));

            String url = userService.updateProfilePicture(userDetails.getId(), file);
            return ResponseEntity.ok(url);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/report")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportResponse> reportUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User reporter = userDetails.getUser();
        return ResponseEntity.ok(reportService.createReport(id, request, reporter));
    }

    @PutMapping("/{id}/interests")
    public ResponseEntity<?> updateInterests(@PathVariable Long id,
                                             @RequestBody List<String> interests,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.updateInterests(id, interests, userDetails.getId());
        return ResponseEntity.ok("Interests updated");
    }
    @GetMapping("/{id}/specializations")
    public ResponseEntity<List<SpecializationResponse>> getSpecializations(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSpecializations(id));
    }

    @PostMapping("/me/specializations")
    public ResponseEntity<SpecializationResponse> addSpecialization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody SpecializationRequest req) {
        return ResponseEntity.ok(service.addSpecialization(userDetails.getUser(), req));
    }

    @DeleteMapping("/me/specializations/{id}")
    public ResponseEntity<Void> removeSpecialization(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        service.removeCustomSpecialization(userDetails.getUser(), id);
        return ResponseEntity.noContent().build();
    }

}
