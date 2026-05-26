package com.example.photoGroupe.dto.auth;

import com.example.photoGroupe.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String fullName;

    private String username;

    private String email;

    private String password;

    private String phoneNumber;

    private String location;

    private Role role;

    // only required when role is PHOTOGRAPHER
    private String bio;

    private String portfolioLink;

}
