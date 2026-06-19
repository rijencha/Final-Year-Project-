package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.PackageType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PhotographerPackageRequest {
    private String name;
    private PackageType packageType;
    private BigDecimal price;
    private String description;
    private Integer deliveryDays;
}