package com.example.photoGroupe.dto.follow;

import java.time.LocalDateTime;

/** Lightweight representation of a user in a follower/following list */
public class FollowUserDTO {

    private Long   id;
    private String username;
    private String fullName;
    private String profilePicture;
    private boolean verified;
    private LocalDateTime followedAt;   // when THIS follow relationship was created

    public FollowUserDTO() {}

    public FollowUserDTO(Long id, String username, String fullName,
                         String profilePicture, boolean verified,
                         LocalDateTime followedAt) {
        this.id             = id;
        this.username       = username;
        this.fullName       = fullName;
        this.profilePicture = profilePicture;
        this.verified       = verified;
        this.followedAt     = followedAt;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────

    public Long getId()                     { return id; }
    public String getUsername()             { return username; }
    public String getFullName()             { return fullName; }
    public String getProfilePicture()       { return profilePicture; }
    public boolean isVerified()             { return verified; }
    public LocalDateTime getFollowedAt()    { return followedAt; }

    public void setId(Long id)                          { this.id = id; }
    public void setUsername(String username)            { this.username = username; }
    public void setFullName(String fullName)            { this.fullName = fullName; }
    public void setProfilePicture(String p)             { this.profilePicture = p; }
    public void setVerified(boolean verified)           { this.verified = verified; }
    public void setFollowedAt(LocalDateTime followedAt) { this.followedAt = followedAt; }
}