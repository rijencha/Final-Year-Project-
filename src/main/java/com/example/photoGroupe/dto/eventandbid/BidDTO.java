package com.example.photoGroupe.dto.eventandbid;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidDTO {
    private BigDecimal price;
    private String proposal;
    private String servicesIncluded;
    private Integer deliveryDays;
}
