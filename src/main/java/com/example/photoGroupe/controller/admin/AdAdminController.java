package com.example.photoGroupe.controller.admin;


import com.example.photoGroupe.dto.ads.BannerAdResponse;
import com.example.photoGroupe.dto.ads.BoostResponse;
import com.example.photoGroupe.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/ads")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdAdminController {

    private final AdminService adAdminService;

    // ── Banners ──

    @GetMapping("/banners/review-queue")
    public ResponseEntity<Page<BannerAdResponse>> getReviewQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adAdminService.getReviewQueue(page, size));
    }

    @GetMapping("/banners")
    public ResponseEntity<Page<BannerAdResponse>> getAllBanners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adAdminService.getAllBanners(page, size));
    }

    @PostMapping("/banners/{id}/approve")
    public ResponseEntity<BannerAdResponse> approveBanner(@PathVariable Long id) {
        return ResponseEntity.ok(adAdminService.approveBanner(id));
    }

    @PostMapping("/banners/{id}/reject")
    public ResponseEntity<BannerAdResponse> rejectBanner(@PathVariable Long id,
                                                         @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adAdminService.rejectBanner(id, reason));
    }

    @PutMapping("/banners/{bannerId}/remove")
    public ResponseEntity<BannerAdResponse> removeBanner(
            @PathVariable Long bannerId,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adAdminService.removeBanner(bannerId, reason));
    }

    // ── Boosts ──

    @GetMapping("/boosts")
    public ResponseEntity<Page<BoostResponse>> getAllBoosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adAdminService.getAllBoosts(page, size));
    }

    @PostMapping("/boosts/{id}/revoke")
    public ResponseEntity<BoostResponse> revokeBoost(@PathVariable Long id,
                                                     @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adAdminService.revokeBoost(id, reason));
    }

    // ── Revenue ──

    @GetMapping("/revenue")
    public ResponseEntity<?> getRevenue() {
        return ResponseEntity.ok(adAdminService.getRevenueSummary());
    }
}