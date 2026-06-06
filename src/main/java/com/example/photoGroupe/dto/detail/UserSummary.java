package com.example.photoGroupe.dto.detail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserSummary {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String bio;
    private boolean enabled;
    private boolean verified;
    private String verificationStatus;
    private boolean accountNonLocked;   // ✅ add (ban/suspend)
    private boolean deleted;
    private String profilePicture;
    private LocalDateTime joinedAt;
    private long pinCount;
}
