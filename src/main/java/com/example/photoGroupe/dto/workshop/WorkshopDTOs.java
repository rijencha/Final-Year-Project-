package com.example.photoGroupe.dto.workshop;

import com.example.photoGroupe.model.workshop.WorkshopParticipantStatus;
import com.example.photoGroupe.model.workshop.WorkshopStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ─── Request DTOs ─────────────────────────────────────────────────────────────

public class WorkshopDTOs {

    public record WorkshopRequest(
            String title,
            String description,
            LocalDateTime workshopDate,
            String location,
            String duration,
            int totalSeats,
            BigDecimal price
    ) {}

    // ─── Response DTOs ────────────────────────────────────────────────────────

    public record WorkshopSummaryResponse(
            Long id,
            String title,
            LocalDateTime workshopDate,
            String location,
            String duration,
            BigDecimal price,
            int totalSeats,
            int seatsAvailable,
            String coverImage,
            WorkshopStatus status,
            PhotographerInfo photographer
    ) {}

    public record WorkshopDetailResponse(
            Long id,
            String title,
            String description,
            LocalDateTime workshopDate,
            String location,
            String duration,
            BigDecimal price,
            int totalSeats,
            int seatsAvailable,
            String coverImage,
            WorkshopStatus status,
            PhotographerInfo photographer,
            LocalDateTime createdAt
    ) {}

    public record PhotographerInfo(
            Long id,
            String name,
            String avatar,
            String username
    ) {}

    public record WorkshopEsewaFormData(
            String totalAmount,
            String taxAmount,
            String productServiceCharge,
            String transactionUuid,
            String productCode,
            String productDeliveryCharge,
            String successUrl,
            String failureUrl,
            String signedFieldNames,
            String signature,
            String paymentUrl
    ) {}

    public record ParticipantResponse(
            Long participantId,
            String fullName,
            String email,
            String username,
            String profilePicture,
            WorkshopParticipantStatus status,
            LocalDateTime registeredAt,
            LocalDateTime paidAt
    ) {}

    public record WorkshopRegistrationRequest(
            String registrantName,
            String registrantEmail,
            String registrantPhone,
            String notes
    ) {}
}