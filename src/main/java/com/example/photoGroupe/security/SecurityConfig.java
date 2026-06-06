package com.example.photoGroupe.security;


import com.example.photoGroupe.security.oauth2.CustomOAuth2UserService;
import com.example.photoGroupe.security.oauth2.OAuth2FailureHandler;
import com.example.photoGroupe.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
//    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // ── Public endpoints (FIRST) ──────────────────────────────
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // ← allow preflight
                        .requestMatchers("/ws/**").permitAll()   // WebSocket handshake
                                .requestMatchers("/api/test/**").permitAll()

                        .requestMatchers("/auth/**").permitAll()                  // ← already there
                        .requestMatchers("/auth/login").permitAll()               // ← add explicit
                        .requestMatchers("/auth/register").permitAll()            // ← add explicit
                        .requestMatchers("/auth/refresh").permitAll()             // ← add explicit
                        .requestMatchers(
                                "/auth/forgot-password",   // ← add
                                "/auth/verify-otp",        // ← add
                                "/auth/reset-password"     // ← add
                        ).permitAll()

                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()

                        // ── Role-based endpoints (MIDDLE) ─────────────────────────
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/photographer/**").hasAnyRole("PHOTOGRAPHER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/users/**").authenticated()

                        // ── Catch-all (ALWAYS LAST) ───────────────────────────────
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(e -> e
                                .baseUri("/oauth2/authorize"))        // ← added
                        .redirectionEndpoint(e -> e
                                .baseUri("/login/oauth2/code/*"))     // ← added
                        .userInfoEndpoint(u -> u.userService(oAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
