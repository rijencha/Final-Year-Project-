// model/OtpToken.java
package com.example.photoGroupe.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructors
    public OtpToken() {}

    public OtpToken(String email, String otp, LocalDateTime expiresAt) {
        this.email = email;
        this.otp = otp;
        this.expiresAt = expiresAt;
    }

    // Getters & Setters
    public Long getId()                         { return id; }
    public String getEmail()                    { return email; }
    public String getOtp()                      { return otp; }
    public LocalDateTime getExpiresAt()         { return expiresAt; }
    public boolean isUsed()                     { return used; }
    public LocalDateTime getCreatedAt()         { return createdAt; }

    public void setEmail(String email)          { this.email = email; }
    public void setOtp(String otp)              { this.otp = otp; }
    public void setExpiresAt(LocalDateTime t)   { this.expiresAt = t; }
    public void setUsed(boolean used)           { this.used = used; }
}