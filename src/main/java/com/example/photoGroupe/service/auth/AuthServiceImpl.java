package com.example.photoGroupe.service.auth;

import com.example.photoGroupe.dto.auth.AuthResponse;
import com.example.photoGroupe.dto.auth.LoginRequest;
import com.example.photoGroupe.dto.auth.RegisterRequest;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.model.*;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.security.JwtService;
import com.example.photoGroupe.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;


    //    private final JwtService jwtService;
    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .message("Email already exists")
                    .build();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return AuthResponse.builder()
                    .message("Username already exists")
                    .build();
        }

        if (request.getRole() == Role.ADMIN || request.getRole() == Role.SUPER_ADMIN) {
            return AuthResponse.builder()
                    .message("You are not allowed to register as ADMIN")
                    .build();
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setLocation(request.getLocation());
        user.setRole(request.getRole());
        user.setEnabled(true);           // ← must be true
        user.setAccountNonLocked(true);  // ← must be true
        user.setVerified(false);

        if (request.getRole() == Role.PHOTOGRAPHER) {
            if (request.getPortfolioLink() == null || request.getPortfolioLink().isEmpty()) {
                return AuthResponse.builder()
                        .message("Portfolio link is required for photographers")
                        .build();
            }
            user.setPortfolioLink(request.getPortfolioLink());
            user.setBio(request.getBio());
            user.setVerified(false);                          // not verified yet
            user.setOauthProvider(OAuthProvider.LOCAL);
            user.setVerificationStatus(VerificationStatus.PENDING); // waiting for admin
        }

        userRepository.save(user);

        // ── Generate tokens ───────────────────────────────────────────────
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken(), "Registration successful");
    }

    // ─── Login ────────────────────────────────────────────────────────────

//    @Override
//    public AuthResponse login(LoginRequest request) {
//
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if(user.getOauthProvider() != OAuthProvider.LOCAL) {
//            return AuthResponse.builder()
//                    .message("You are not allowed to log in")
//                    .build();
//        }
//
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                        request.getEmail(),
//                        request.getPassword()
//                )
//        );
//        // ── Generate tokens ───────────────────────────────────────────────
//        String accessToken = jwtService.generateAccessToken(user);
//        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
//        return buildAuthResponse(user, accessToken, refreshToken.getToken(), "Login successful");
//    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ── Generate tokens ───────────────────────────────────────────────
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken(), "Login successful");
    }

    // ─── Refresh Token ────────────────────────────────────────────────────

    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue);
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        return buildAuthResponse(user, newAccessToken, refreshToken.getToken(), "Token refreshed");
    }

    // ─── Logout ───────────────────────────────────────────────────────────

    public void logout(String refreshTokenValue) {
        refreshTokenService.revokeToken(refreshTokenValue);
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user, String accessToken,
                                           String refreshToken, String message) {

        UserSummary userSummary = UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getActualUsername())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .verified(user.isVerified())
                .verificationStatus(
                        user.getVerificationStatus() != null
                                ? user.getVerificationStatus().name()
                                : null
                )
                .build();

        return AuthResponse.builder()
                .token(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .user(userSummary)      // ← nested
                .message(message)
                .build();
    }
}
