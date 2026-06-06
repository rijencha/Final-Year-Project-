package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.model.booking.PaymentStatus;
import com.example.photoGroupe.model.event.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private String eventTitle;
    private EventType eventType;
    private LocalDateTime eventDate;
    private String location;
    private String description;
    private Integer durationHours;
    private BigDecimal price;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String specialRequests;
    private String rejectionReason;
    private String cancellationReason;
    private LocalDateTime createdAt;

    // Client info
    private Long clientId;
    private String clientName;
    private String clientAvatar;

    // Photographer info
    private Long photographerId;
    private String photographerName;
    private String photographerAvatar;
    private String portfolioLink;
    private boolean photographerVerified;

    public BookingResponse(Booking b) {
        this.id                   = b.getId();
        this.eventTitle           = b.getEventTitle();
        this.eventType            = b.getEventType();
        this.eventDate            = b.getEventDate();
        this.location             = b.getLocation();
        this.description          = b.getDescription();
        this.durationHours        = b.getDurationHours();
        this.price                = b.getPrice();
        this.status               = b.getStatus();
        this.paymentStatus        = b.getPaymentStatus();
        this.specialRequests      = b.getSpecialRequests();
        this.rejectionReason      = b.getRejectionReason();
        this.cancellationReason   = b.getCancellationReason();
        this.createdAt            = b.getCreatedAt();

        this.clientId             = b.getClient().getId();
        this.clientName           = b.getClient().getFullName();
        this.clientAvatar         = b.getClient().getProfilePicture();

        this.photographerId       = b.getPhotographer().getId();
        this.photographerName     = b.getPhotographer().getFullName();
        this.photographerAvatar   = b.getPhotographer().getProfilePicture();
        this.portfolioLink        = b.getPhotographer().getPortfolioLink();
        this.photographerVerified = b.getPhotographer().isVerified();
    }

    // All getters
    public Long getId()                       { return id; }
    public String getEventTitle()             { return eventTitle; }
    public EventType getEventType()           { return eventType; }
    public LocalDateTime getEventDate()       { return eventDate; }
    public String getLocation()               { return location; }
    public String getDescription()            { return description; }
    public Integer getDurationHours()         { return durationHours; }
    public BigDecimal getPrice()              { return price; }
    public BookingStatus getStatus()          { return status; }
    public PaymentStatus getPaymentStatus()   { return paymentStatus; }
    public String getSpecialRequests()        { return specialRequests; }
    public String getRejectionReason()        { return rejectionReason; }
    public String getCancellationReason()     { return cancellationReason; }
    public LocalDateTime getCreatedAt()       { return createdAt; }
    public Long getClientId()                 { return clientId; }
    public String getClientName()             { return clientName; }
    public String getClientAvatar()           { return clientAvatar; }
    public Long getPhotographerId()           { return photographerId; }
    public String getPhotographerName()       { return photographerName; }
    public String getPhotographerAvatar()     { return photographerAvatar; }
    public String getPortfolioLink()          { return portfolioLink; }
    public boolean isPhotographerVerified()   { return photographerVerified; }
}
