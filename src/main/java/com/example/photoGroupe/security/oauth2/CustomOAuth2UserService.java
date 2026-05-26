package com.example.photoGroupe.security.oauth2;

import com.example.photoGroupe.model.OAuthProvider;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String registrationId = request.getClientRegistration().getRegistrationId(); // "google" or "facebook"
        OAuthProvider provider = OAuthProvider.valueOf(registrationId.toUpperCase());

        String oauthId  = oAuth2User.getName(); // provider's stable user ID
        String email    = extractEmail(oAuth2User, registrationId);
        String fullName = extractName(oAuth2User, registrationId);
        String picture  = extractPicture(oAuth2User, registrationId);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not returned by provider");
        }

        User user = userRepository.findByOauthProviderAndOauthId(provider, oauthId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> handleExistingEmail(existing, provider, oauthId))
                        .orElseGet(() -> createNewOAuthUser(email, fullName, picture, provider, oauthId)));

        userRepository.save(user);
        return new CustomOAuth2UserPrincipal(user, oAuth2User.getAttributes());
    }

    private User handleExistingEmail(User existing, OAuthProvider provider, String oauthId) {
        if (existing.getOauthProvider() == OAuthProvider.LOCAL) {
            // Email exists as a password account → link it (auto-login)
            existing.setOauthProvider(provider);
            existing.setOauthId(oauthId);
        }
        // If already the same provider, just return as-is
        return existing;
    }

    private User createNewOAuthUser(String email, String fullName, String picture,
                                    OAuthProvider provider, String oauthId) {
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setUsername(generateUsername(email));
        user.setProfilePicture(picture);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // unusable password
        user.setRole(Role.USER); // default role
        user.setOauthProvider(provider);
        user.setOauthId(oauthId);
        user.setEnabled(true);
        user.setVerified(false);
        return user;
    }

    // ── Attribute extraction ───────────────────────────────────────────────

    private String extractEmail(OAuth2User user, String provider) {
        return switch (provider) {
            case "google"   -> user.getAttribute("email");
            case "facebook" -> user.getAttribute("email");
            default         -> null;
        };
    }

    private String extractName(OAuth2User user, String provider) {
        return switch (provider) {
            case "google"   -> user.getAttribute("name");
            case "facebook" -> user.getAttribute("name");
            default         -> "Unknown";
        };
    }

    private String extractPicture(OAuth2User user, String provider) {
        return switch (provider) {
            case "google"   -> user.getAttribute("picture");
            case "facebook" -> {
                Map<String, Object> picture = user.getAttribute("picture");
                if (picture != null) {
                    Map<String, Object> data = (Map<String, Object>) picture.get("data");
                    yield data != null ? (String) data.get("url") : null;
                }
                yield null;
            }
            default -> null;
        };
    }

    private String generateUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9_]", "");
        String candidate = base;
        int i = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }
}