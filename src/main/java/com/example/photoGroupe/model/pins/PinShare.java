package com.example.photoGroupe.model.pins;

import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "pin_shares")
public class PinShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id", nullable = false)
    private Pin pin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by", nullable = false)
    private User sharedBy;

    @Column(name = "share_link", nullable = false)
    private String shareLink;          // e.g. https://yourapp.com/pin/42

    @Column(name = "shared_at", updatable = false)
    private LocalDateTime sharedAt;

    @PrePersist
    protected void onCreate() { this.sharedAt = LocalDateTime.now(); }

    public PinShare() {}

    public PinShare(Pin pin, User sharedBy, String shareLink) {
        this.pin       = pin;
        this.sharedBy  = sharedBy;
        this.shareLink = shareLink;
    }

    public Long getId()               { return id; }
    public Pin getPin()               { return pin; }
    public User getSharedBy()         { return sharedBy; }
    public String getShareLink()      { return shareLink; }
    public LocalDateTime getSharedAt(){ return sharedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PinShare)) return false;
        return Objects.equals(id, ((PinShare) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}