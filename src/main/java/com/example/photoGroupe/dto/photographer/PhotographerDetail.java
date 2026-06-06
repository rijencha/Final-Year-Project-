package com.example.photoGroupe.dto.photographer;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PhotographerDetail {
    private Long id;
    private String fullName;
    private String username;
    private String bio;
    private String location;
    private String profilePicture;
    private boolean verified;
    private boolean enable;
    private boolean accountNonLocked;   // ✅ add (ban/suspend)
    private boolean deleted;
    private LocalDateTime joinedAt;
    private String phoneNumber;
    private long pinCount;
}