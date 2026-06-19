package com.example.photoGroupe.model.booking;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "photographer_packages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotographerPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "photographer_id", nullable = false)
    private User photographer;

    @Column(nullable = false)
    private String name;            // e.g. "Wedding Basic", "Portrait Premium"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PackageType packageType; // PREMIUM or CUSTOM

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 1000)
    private String description;

    @Column(name = "delivery_days")
    private Integer deliveryDays;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // Getters
    public Long getId()                  { return id; }
    public User getPhotographer()        { return photographer; }
    public String getName()              { return name; }
    public PackageType getPackageType()  { return packageType; }
    public BigDecimal getPrice()         { return price; }
    public String getDescription()       { return description; }
    public Integer getDeliveryDays()     { return deliveryDays; }
    public LocalDateTime getCreatedAt()  { return createdAt; }

    // Setters
    public void setPhotographer(User u)      { this.photographer = u; }
    public void setName(String n)            { this.name = n; }
    public void setPackageType(PackageType t){ this.packageType = t; }
    public void setPrice(BigDecimal p)       { this.price = p; }
    public void setDescription(String d)     { this.description = d; }
    public void setDeliveryDays(Integer d)   { this.deliveryDays = d; }
}