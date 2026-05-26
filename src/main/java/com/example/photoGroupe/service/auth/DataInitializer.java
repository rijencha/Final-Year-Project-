package com.example.photoGroupe.service.auth;

import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createSuperAdminIfNotExists();
    }

    @Value("${superadmin.email}")
    private String superAdminEmail;

    @Value("${superadmin.username}")
    private String superAdminUsername;

    @Value("${superadmin.password}")
    private String superAdminPassword;

    private void createSuperAdminIfNotExists() {

        if (userRepository.existsByEmail(superAdminEmail)) {
            log.info("Super admin already exists, skipping creation.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setFullName("Super Admin");
        superAdmin.setEmail(superAdminEmail);
        superAdmin.setUsername(superAdminUsername);
        superAdmin.setPassword(passwordEncoder.encode(superAdminPassword)); // change this
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setVerified(true);
        superAdmin.setEnabled(true);

        userRepository.save(superAdmin);
        log.info("Super admin created successfully with email: {}", superAdminEmail);
    }
}
