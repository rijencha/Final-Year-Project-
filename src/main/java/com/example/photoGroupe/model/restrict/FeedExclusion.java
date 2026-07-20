package com.example.photoGroupe.model.restrict;

import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.workshop.Workshop;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "feed_exclusions")
public class FeedExclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who marked something as "not interested". */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeedExclusionScope scope;

    /** Set only when scope == PIN */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pin_id")
    private Pin pin;

    /** Set only when scope == USER */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excluded_user_id")
    private User excludedUser;

    /** Set only when scope == CATEGORY */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /** Set only when scope == WORKSHOP */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    /** Set only when scope == EVENT_REQUEST */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_request_id")
    private EventRequest eventRequest;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public FeedExclusion() {}

    /**
     * @deprecated use the 7-arg constructor covering all scopes (WORKSHOP / EVENT_REQUEST included).
     * Kept temporarily so existing call sites still compile during migration.
     */
    @Deprecated
    public FeedExclusion(User owner, FeedExclusionScope scope, Pin pin, User excludedUser, Category category) {
        this(owner, scope, pin, excludedUser, category, null, null);
    }

    public FeedExclusion(User owner, FeedExclusionScope scope, Pin pin, User excludedUser,
                         Category category, Workshop workshop, EventRequest eventRequest) {
        this.owner        = owner;
        this.scope        = scope;
        this.pin          = pin;
        this.excludedUser = excludedUser;
        this.category     = category;
        this.workshop     = workshop;
        this.eventRequest = eventRequest;
    }

    // ─── Getters / Setters ────────────────────────────────────────────────

    public Long getId()                       { return id; }
    public User getOwner()                    { return owner; }
    public FeedExclusionScope getScope()      { return scope; }
    public Pin getPin()                       { return pin; }
    public User getExcludedUser()             { return excludedUser; }
    public Category getCategory()             { return category; }
    public Workshop getWorkshop()             { return workshop; }
    public EventRequest getEventRequest()     { return eventRequest; }
    public LocalDateTime getCreatedAt()       { return createdAt; }

    public void setOwner(User owner)                     { this.owner = owner; }
    public void setScope(FeedExclusionScope scope)       { this.scope = scope; }
    public void setPin(Pin pin)                          { this.pin = pin; }
    public void setExcludedUser(User excludedUser)       { this.excludedUser = excludedUser; }
    public void setCategory(Category category)           { this.category = category; }
    public void setWorkshop(Workshop workshop)           { this.workshop = workshop; }
    public void setEventRequest(EventRequest eventRequest) { this.eventRequest = eventRequest; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeedExclusion)) return false;
        return Objects.equals(id, ((FeedExclusion) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}