package com.example.photoGroupe.dto.ads;

import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.BannerSlot;
import com.example.photoGroupe.model.ads.BannerStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BannerAdResponse(
        Long id,
        String title,
        String targetUrl,
        String imageUrl,
        BannerSlot slot,
        int daysPurchased,
        BigDecimal amountPaid,
        BannerStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        Long advertiserId,
        String advertiserUsername
) {
    public static BannerAdResponse from(BannerAd b) {
        return new BannerAdResponse(
                b.getId(), b.getTitle(), b.getTargetUrl(), b.getImageUrl(),
                b.getSlot(), b.getDaysPurchased(), b.getAmountPaid(), b.getStatus(),
                b.getStartAt(), b.getEndAt(), b.getCreatedAt(),
                b.getAdvertiser().getId(), b.getAdvertiser().getActualUsername()
        );
    }
}
