package com.example.photoGroupe.model.workshop;

import com.example.photoGroupe.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Join table between Workshop and User (participant).
 * One row per registration; payment info tracked here.
 */
@Entity
@Table(
        name = "workshop_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_workshop_participant",
                columnNames = {"workshop_id", "participant_id"}
        )
)
public class WorkshopParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Relationships ────────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workshop_id", nullable = false)
    private Workshop workshop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private User participant;

    @Column(name = "registrant_name", nullable = false)
    private String registrantName;

    @Column(name = "registrant_email", nullable = false)
    private String registrantEmail;

    @Column(name = "registrant_phone", nullable = false)
    private String registrantPhone;

    @Column(name = "notes")
    private String notes;

    // ─── Payment ──────────────────────────────────────────────────────────

    /** eSewa transaction UUID — set after initiatePayment */
    @Column(name = "transaction_uuid", unique = true)
    private String transactionUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkshopParticipantStatus status = WorkshopParticipantStatus.PENDING_PAYMENT;

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "registered_at", updatable = false)
    private LocalDateTime registeredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                                 { return id; }
    public Workshop getWorkshop()                       { return workshop; }
    public User getParticipant()                        { return participant; }
    public String getTransactionUuid()                  { return transactionUuid; }
    public WorkshopParticipantStatus getStatus()        { return status; }
    public LocalDateTime getRegisteredAt()              { return registeredAt; }
    public LocalDateTime getPaidAt()                    { return paidAt; }

    public String getRegistrantName() {
        return registrantName;
    }

    public String getRegistrantEmail() {
        return registrantEmail;
    }

    public String getRegistrantPhone() {
        return registrantPhone;
    }

    public String getNotes() {
        return notes;
    }

    public void setWorkshop(Workshop workshop)                      { this.workshop = workshop; }
    public void setParticipant(User participant)                    { this.participant = participant; }
    public void setTransactionUuid(String transactionUuid)         { this.transactionUuid = transactionUuid; }
    public void setStatus(WorkshopParticipantStatus status)        { this.status = status; }
    public void setPaidAt(LocalDateTime paidAt)                    { this.paidAt = paidAt; }

    public void setRegistrantName(String registrantName) {
        this.registrantName = registrantName;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setRegistrantPhone(String registrantPhone) {
        this.registrantPhone = registrantPhone;
    }
    public void setRegistrantEmail(String registrantEmail) {
        this.registrantEmail = registrantEmail;
    }

}