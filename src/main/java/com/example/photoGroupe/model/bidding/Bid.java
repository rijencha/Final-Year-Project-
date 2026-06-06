package com.example.photoGroupe.model.bidding;

import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// model/Bid.java
// model/Bid.java
@Entity
@Table(name = "bids")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_request_id", nullable = false)
    private EventRequest eventRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String proposal;            // photographer's pitch

    private String servicesIncluded;    // "4hrs shooting, 100 edited photos"

    private Integer deliveryDays;       // estimated delivery in days

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;           // PENDING, ACCEPTED, REJECTED, WITHDRAWN

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status    = BidStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    // Getters & Setters
    public Long getId()                       { return id; }
    public EventRequest getEventRequest()     { return eventRequest; }
    public User getPhotographer()             { return photographer; }
    public BigDecimal getPrice()              { return price; }
    public String getProposal()               { return proposal; }
    public String getServicesIncluded()       { return servicesIncluded; }
    public Integer getDeliveryDays()          { return deliveryDays; }
    public BidStatus getStatus()              { return status; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public void setPrice(BigDecimal p)        { this.price = p; }
    public void setProposal(String p)         { this.proposal = p; }
    public void setServicesIncluded(String s) { this.servicesIncluded = s; }
    public void setDeliveryDays(Integer d)    { this.deliveryDays = d; }
    public void setStatus(BidStatus s)        { this.status = s; }
}
