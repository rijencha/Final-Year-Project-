package com.example.photoGroupe.dto.photographer;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PhotographerVerificationResponse {
    private Long id;
    private String fullName;
    private String email;
    private String username;
    private String portfolioLink;
    private String bio;
    private String location;
    private String verificationStatus;
}
