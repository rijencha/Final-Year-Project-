package com.example.photoGroupe.dto.eventandbid;

import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.bidding.BidStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidResponse {
    private Long id;
    private Long eventRequestId;
    private BigDecimal price;
    private String proposal;
    private String servicesIncluded;
    private Integer deliveryDays;
    private BidStatus status;
    private LocalDateTime createdAt;

    // photographer info
    private Long photographerId;
    private String photographerName;
    private String photographerAvatar;
    private String portfolioLink;
    private boolean verified;

    public BidResponse(Bid b) {
        this.id               = b.getId();
        this.eventRequestId   = b.getEventRequest().getId();
        this.price            = b.getPrice();
        this.proposal         = b.getProposal();
        this.servicesIncluded = b.getServicesIncluded();
        this.deliveryDays     = b.getDeliveryDays();
        this.status           = b.getStatus();
        this.createdAt        = b.getCreatedAt();
        this.photographerId   = b.getPhotographer().getId();
        this.photographerName = b.getPhotographer().getFullName();
        this.photographerAvatar = b.getPhotographer().getProfilePicture();
        this.portfolioLink    = b.getPhotographer().getPortfolioLink();
        this.verified         = b.getPhotographer().isVerified();
    }
    // getters
}
