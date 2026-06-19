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

    public Long getId() {
        return id;
    }

    public Long getEventRequestId() {
        return eventRequestId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getProposal() {
        return proposal;
    }

    public String getServicesIncluded() {
        return servicesIncluded;
    }

    public Integer getDeliveryDays() {
        return deliveryDays;
    }

    public BidStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getPhotographerId() {
        return photographerId;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public String getPhotographerAvatar() {
        return photographerAvatar;
    }

    public String getPortfolioLink() {
        return portfolioLink;
    }

    public boolean isVerified() {
        return verified;
    }
}
