package com.example.photoGroupe.model.ads;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "banner_ads")
public class BannerAd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    @Column(nullable = false)
    private String title;

    @Column(name = "target_url", nullable = false)
    private String targetUrl;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "image_public_id")
    private String imagePublicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerSlot slot;

    @Column(name = "days_purchased", nullable = false)
    private int daysPurchased;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "transaction_uuid", unique = true)
    private String transactionUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BannerStatus status;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public User getAdvertiser() { return advertiser; }
    public void setAdvertiser(User advertiser) { this.advertiser = advertiser; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getImagePublicId() { return imagePublicId; }
    public void setImagePublicId(String imagePublicId) { this.imagePublicId = imagePublicId; }
    public BannerSlot getSlot() { return slot; }
    public void setSlot(BannerSlot slot) { this.slot = slot; }
    public int getDaysPurchased() { return daysPurchased; }
    public void setDaysPurchased(int daysPurchased) { this.daysPurchased = daysPurchased; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public String getTransactionUuid() { return transactionUuid; }
    public void setTransactionUuid(String transactionUuid) { this.transactionUuid = transactionUuid; }
    public BannerStatus getStatus() { return status; }
    public void setStatus(BannerStatus status) { this.status = status; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}