package com.example.photoGroupe.dto.booking;

import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.model.booking.EscrowStatus;
import com.example.photoGroupe.model.booking.PaymentStatus;
import com.example.photoGroupe.model.event.EventType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private String eventTitle;
    private EventType eventType;
    private String customEventType;
    private String displayEventType;
    private LocalDateTime eventDate;
    private String location;
    private String description;
    private Integer durationHours;
    private BigDecimal price;
    private EscrowStatus escrowStatus;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String specialRequests;
    private String rejectionReason;
    private String cancellationReason;
    private LocalDateTime createdAt;

    private String clientEmail;
    private String clientPhone;
    private String clientLocation;

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

    private boolean hasPendingPayment;

    public BookingResponse(Booking b) {
        this.id                   = b.getId();
        this.eventTitle           = b.getEventTitle();
        this.eventType            = b.getEventType();
        this.customEventType = b.getCustomEventType();
        this.displayEventType = b.getEventType() != null
                ? b.getEventType().name()
                : b.getCustomEventType();
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
        this.escrowStatus = b.getEscrowStatus();

        this.clientId             = b.getClient().getId();
        this.clientName           = b.getClient().getFullName();
        this.clientAvatar         = b.getClient().getProfilePicture();

        this.photographerId       = b.getPhotographer().getId();
        this.photographerName     = b.getPhotographer().getFullName();
        this.photographerAvatar   = b.getPhotographer().getProfilePicture();
        this.portfolioLink        = b.getPhotographer().getPortfolioLink();
        this.photographerVerified = b.getPhotographer().isVerified();

        this.clientEmail    = b.getClient().getEmail();
        this.clientPhone    = b.getClient().getPhoneNumber();
        this.clientLocation = b.getClient().getLocation();
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
    public EscrowStatus getEscrowStatus() { return escrowStatus; }
    public String getClientEmail() {
        return clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public String getClientLocation() {
        return clientLocation;
    }

    public String getCustomEventType() {
        return customEventType;
    }

    public String getDisplayEventType() {
        return displayEventType;
    }

    public boolean isHasPendingPayment() { return hasPendingPayment; }

    public void setHasPendingPayment(boolean hasPendingPayment) {
        this.hasPendingPayment = hasPendingPayment;
    }
}
