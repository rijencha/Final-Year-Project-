package com.example.photoGroupe.controller.user;

import com.example.photoGroupe.dto.ads.BannerAdResponse;
import com.example.photoGroupe.dto.ads.BoostResponse;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.BannerSlot;
import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.model.ads.PhotographerBoost;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.ads.BannerAdRepository;
import com.example.photoGroupe.repo.ads.PhotographerBoostRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.ads.AdPricingConfig;
import com.example.photoGroupe.service.payment.EsewaPaymentService;
import com.example.photoGroupe.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/users/ads")
@RequiredArgsConstructor
public class AdController {

    private final BannerAdRepository bannerAdRepository;
    private final PhotographerBoostRepository boostRepository;
    private final CloudinaryService cloudinaryService;
    private final AdPricingConfig adPricingConfig;

    @PostMapping("/banners")
    public ResponseEntity<BannerAdResponse> createBanner(
            @RequestParam MultipartFile image,
            @RequestParam String title,
            @RequestParam String targetUrl,
            @RequestParam BannerSlot slot,
            @RequestParam int days,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {
        String url = cloudinaryService.uploadProfilePicture(image, currentUser.getUser().getId()); // or a dedicated ad-upload method
        BannerAd banner = new BannerAd();
        banner.setAdvertiser(currentUser.getUser());
        banner.setTitle(title);
        banner.setTargetUrl(targetUrl);
        banner.setSlot(slot);
        banner.setDaysPurchased(days);
        banner.setAmountPaid(adPricingConfig.bannerPrice(days));
        banner.setImageUrl(url);
        banner.setStatus(BannerStatus.PENDING_PAYMENT);
        bannerAdRepository.save(banner);
        return ResponseEntity.ok(BannerAdResponse.from(banner));
    }

    @GetMapping("/banners")
    public ResponseEntity<?> getActiveBanners(@RequestParam BannerSlot slot) {
        return ResponseEntity.ok(bannerAdRepository.findActiveBySlot(slot, LocalDateTime.now()));
    }

    @PostMapping("/boosts")
    public ResponseEntity<BoostResponse> createBoost(@RequestParam int days,
                                         @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser.getUser().getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("Only photographers can purchase a boost");

        PhotographerBoost boost = new PhotographerBoost();
        boost.setPhotographer(currentUser.getUser());
        boost.setDaysPurchased(days);
        boost.setAmountPaid(adPricingConfig.boostPrice(days));
        boost.setStatus(BannerStatus.PENDING_PAYMENT);
        boostRepository.save(boost);
        return ResponseEntity.ok(BoostResponse.from(boost));
    }

    @GetMapping("/boosts/history")
    public ResponseEntity<List<BoostResponse>> getBoostHistory(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<BoostResponse> history = boostRepository
                .findByPhotographerIdOrderByCreatedAtDesc(currentUser.getUser().getId())
                .stream()
                .map(BoostResponse::from)
                .toList();
        return ResponseEntity.ok(history);
    }

}
