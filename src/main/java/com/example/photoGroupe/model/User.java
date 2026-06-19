package com.example.photoGroupe.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Core Identity ────────────────────────────────────────────────────

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "interests")
    private String interests;

    // ─── OAUTH ─────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OAuthProvider oauthProvider = OAuthProvider.LOCAL;

    @Column(name = "oauth_id")
    private String oauthId; // provider's user ID (stable, unlike email)

    // ─── Role ─────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ─── Profile ──────────────────────────────────────────────────────────

    @Column(name = "profile_picture")
    private String profilePicture;

    private String bio;

    private String location;

    @Column(name = "phone_number")
    private String phoneNumber;

    // ─── Account Status ───────────────────────────────────────────────────

    /**
     * Photographer-specific: set to true once Admin approves verification.
     * Always false for USER, ADMIN, SUPER_ADMIN.
     */
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "portfolio_link")
    private String portfolioLink;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    /**
     * Controlled by Admin/SuperAdmin to ban or suspend a user.
     */
    @Column(name = "is_enabled", nullable = false)
    private boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;

    /**
     * Controlled by Admin/SuperAdmin to lock suspicious accounts.
     */
    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    // ─── Timestamps ───────────────────────────────────────────────────────

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ─── UserDetails (Spring Security) ───────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // email used as login identifier
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ─── Constructors ─────────────────────────────────────────────────────

    public User() {}

    public User(String fullName, String email, String password, String username, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

// ─── Getters ──────────────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getFullName()             { return fullName; }
    public String getEmail()                { return email; }
    public String getActualUsername()       { return username; } // avoids clash with UserDetails
    public Role getRole()                   { return role; }
    public String getProfilePicture()       { return profilePicture; }
    public String getBio()                  { return bio; }
    public String getLocation()             { return location; }
    public String getPhoneNumber()          { return phoneNumber; }
    public boolean isVerified()             { return verified; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }
    public boolean isDeleted()              { return deleted; }
    public LocalDateTime getDeletedAt()     { return deletedAt; }
    public String getPortfolioLink() {
        return portfolioLink;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public OAuthProvider getOauthProvider() {
        return oauthProvider;
    }
    public String getOauthId() {
        return oauthId;
    }
    public String getInterests() { return interests; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }

    // setter
    public void setYearsOfExperience(Integer yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
// ─── Setters ──────────────────────────────────────────────────────────
    public void setInterests(String interests) { this.interests = interests; }
    public void setId(Long id)                              { this.id = id; }
    public void setFullName(String fullName)                { this.fullName = fullName; }
    public void setEmail(String email)                      { this.email = email; }
    public void setPassword(String password)                { this.password = password; }
    public void setUsername(String username)                { this.username = username; }
    public void setRole(Role role)                          { this.role = role; }
    public void setProfilePicture(String profilePicture)    { this.profilePicture = profilePicture; }
    public void setBio(String bio)                          { this.bio = bio; }
    public void setLocation(String location)                { this.location = location; }
    public void setPhoneNumber(String phoneNumber)          { this.phoneNumber = phoneNumber; }
    public void setVerified(boolean verified)               { this.verified = verified; }
    public void setEnabled(boolean enabled)                 { this.enabled = enabled; }
    public void setDeleted(boolean deleted)             { this.deleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt)   { this.deletedAt = deletedAt; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public void setPortfolioLink(String portfolioLink) {
        this.portfolioLink = portfolioLink;
    }
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void setOauthProvider(OAuthProvider oauthProvider) {
        this.oauthProvider = oauthProvider;
    }



    public void setOauthId(String oauthId) {
        this.oauthId = oauthId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    // ─── equals / hashCode / toString ─────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", verified=" + verified +
                ", enabled=" + enabled +
                '}';
    }
}
