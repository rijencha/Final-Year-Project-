package com.example.photoGroupe.dto.auth;

import com.example.photoGroupe.dto.detail.UserSummary;
import lombok.*;

// AuthResponse.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;           // Access token (Bearer)
    private String refreshToken;    // Refresh token  ← ADD THIS
    private String type;            // "Bearer"
    private UserSummary user;
    private String message;
}
