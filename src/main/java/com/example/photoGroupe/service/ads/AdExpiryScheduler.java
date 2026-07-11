package com.example.photoGroupe.service.ads;

import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.repo.ads.BannerAdRepository;
import com.example.photoGroupe.repo.ads.PhotographerBoostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdExpiryScheduler {

    private final BannerAdRepository bannerAdRepository;
    private final PhotographerBoostRepository boostRepository;

    @Scheduled(cron = "0 0 * * * *") // hourly
    @Transactional
    public void expireAds() {
        LocalDateTime now = LocalDateTime.now();
        bannerAdRepository.findByStatusAndEndAtBefore(BannerStatus.ACTIVE, now)
                .forEach(b -> { b.setStatus(BannerStatus.EXPIRED); bannerAdRepository.save(b); });
        boostRepository.findByStatusAndEndAtBefore(BannerStatus.ACTIVE, now)
                .forEach(b -> { b.setStatus(BannerStatus.EXPIRED); boostRepository.save(b); });
    }
}