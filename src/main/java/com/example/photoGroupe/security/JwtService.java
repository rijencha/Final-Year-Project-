package com.example.photoGroupe.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // ─── Key ──────────────────────────────────────────────────────────────

//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(secretKey.getBytes());
//    }
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKey); // ← decode Base64
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ─── Token Generation ─────────────────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Cast to your User entity to embed extra claims
        if (userDetails instanceof com.example.photoGroupe.model.User user) {
            claims.put("userId", user.getId());
            claims.put("role", user.getRole().name());
            claims.put("fullName", user.getFullName());
        }

        return buildToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims,
                              String subject,
                              long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)           // email (your login identifier)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── Token Validation ─────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ─── Claims Extraction ────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
