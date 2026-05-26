package com.example.photoGroupe.dto.detail;

import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @Size(max = 100)
    private String fullName;

    @Size(max = 50)
    private String username;

    @Size(max = 500)
    private String bio;

    @Size(max = 100)
    private String location;

    @Size(max = 20)
    private String phoneNumber;

    // Photographer-only
    @Size(max = 255)
    private String portfolioLink;

    // Getters & Setters
    public String getFullName()        { return fullName; }
    public String getUsername()        { return username; }
    public String getBio()             { return bio; }
    public String getLocation()        { return location; }
    public String getPhoneNumber()     { return phoneNumber; }
    public String getPortfolioLink()   { return portfolioLink; }

    public void setFullName(String fullName)            { this.fullName = fullName; }
    public void setUsername(String username)            { this.username = username; }
    public void setBio(String bio)                      { this.bio = bio; }
    public void setLocation(String location)            { this.location = location; }
    public void setPhoneNumber(String phoneNumber)      { this.phoneNumber = phoneNumber; }
    public void setPortfolioLink(String portfolioLink)  { this.portfolioLink = portfolioLink; }
}