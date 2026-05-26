package com.example.photoGroupe.security.oauth2;

import com.example.photoGroupe.model.RefreshToken;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.JwtService;
import com.example.photoGroupe.security.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOAuth2UserPrincipal principal = (CustomOAuth2UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isNewUser", user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5)))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
