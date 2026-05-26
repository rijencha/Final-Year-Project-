package com.example.photoGroupe.dto.detail;

import jakarta.validation.constraints.NotBlank;

public class UpgradeToPhotographerRequest {

    @NotBlank(message = "Portfolio link is required")
    private String portfolioLink;

    private String bio;

    public String getPortfolioLink() { return portfolioLink; }
    public String getBio()           { return bio; }
}