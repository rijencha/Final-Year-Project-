package com.example.photoGroupe.dto.ads;

import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.model.ads.PhotographerBoost;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoostResponse(
        Long id,
        int daysPurchased,
        BigDecimal amountPaid,
        BannerStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        Long photographerId,
        String photographerName,
        String photographerUsername,
        String photographerProfilePicture
) {
    public static BoostResponse from(PhotographerBoost b) {
        return new BoostResponse(
                b.getId(), b.getDaysPurchased(), b.getAmountPaid(), b.getStatus(),
                b.getStartAt(), b.getEndAt(), b.getCreatedAt(),
                b.getPhotographer().getId(),
                b.getPhotographer().getFullName(),
                b.getPhotographer().getActualUsername(),
                b.getPhotographer().getProfilePicture()
        );
    }
}