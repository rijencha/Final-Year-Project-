package com.example.photoGroupe.dto.admin;

import lombok.Data;

@Data
public class CreateAdminRequest {
    private String fullName;
    private String email;
    private String username;
    private String password;
    private String phoneNumber;
    private String location;
}
