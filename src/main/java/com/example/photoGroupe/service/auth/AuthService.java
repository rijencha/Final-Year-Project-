package com.example.photoGroupe.service.auth;

import com.example.photoGroupe.dto.auth.AuthResponse;
import com.example.photoGroupe.dto.auth.LoginRequest;
import com.example.photoGroupe.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    String sendOtp(String email);

    String verifyOtp(String email, String otp);

    String resetPassword(String email, String otp, String newPassword);

}
