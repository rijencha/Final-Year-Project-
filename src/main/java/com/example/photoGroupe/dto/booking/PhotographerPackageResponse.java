package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.PackageType;
import com.example.photoGroupe.model.booking.PhotographerPackage;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PhotographerPackageResponse {
    private Long id;
    private String name;
    private PackageType packageType;
    private BigDecimal price;
    private String description;
    private Integer deliveryDays;
    private LocalDateTime createdAt;

    public static PhotographerPackageResponse from(PhotographerPackage p) {
        return PhotographerPackageResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .packageType(p.getPackageType())
                .price(p.getPrice())
                .description(p.getDescription())
                .deliveryDays(p.getDeliveryDays())
                .createdAt(p.getCreatedAt())
                .build();
    }
}