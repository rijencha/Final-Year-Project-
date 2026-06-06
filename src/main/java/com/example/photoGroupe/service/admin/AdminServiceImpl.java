package com.example.photoGroupe.service.admin;

import com.example.photoGroupe.dto.admin.CreateAdminRequest;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerVerificationResponse;
import com.example.photoGroupe.model.OAuthProvider;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.repo.AdminRepo;
import com.example.photoGroupe.repo.PinRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final PinRepository pinRepository;
    private final NotificationService notificationService;

    // ─── Admin ────────────────────────────────────────────────────────────

    @Override
    public UserSummary getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toSummary(user);
    }

    @Override
    public List<UserSummary> getAllUsers() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User loggedInUser = customUserDetails.getUser();

        List<User> users;

        // ─── SUPER ADMIN ─────────────────────────────────────

        if (loggedInUser.getRole() == Role.SUPER_ADMIN) {

            users = userRepository.findByRoleInAndDeletedFalse(
                    List.of(
                            Role.SUPER_ADMIN,
                            Role.ADMIN,
                            Role.USER,
                            Role.PHOTOGRAPHER
                    )
            );
        }

        // ─── ADMIN ───────────────────────────────────────────

        else if (loggedInUser.getRole() == Role.ADMIN) {

            users = userRepository.findByRoleInAndDeletedFalse(
                    List.of(
                            Role.USER,
                            Role.PHOTOGRAPHER
                    )
            );
        }

        // ─── INVALID ─────────────────────────────────────────

        else {
            throw new RuntimeException("Unauthorized");
        }

        return users.stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found or already deleted: " + id));

        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        user.setEnabled(false); // prevent login immediately
        userRepository.save(user);
    }

    @Override
    public void hardDeleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserSummary restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!user.isDeleted()) {
            throw new RuntimeException("User is not deleted");
        }

        user.setDeleted(false);
        user.setDeletedAt(null);
        user.setEnabled(true);
        userRepository.save(user);

        return toSummary(user);
    }

    @Override
    public List<UserSummary> getDeletedUsers() {
        return userRepository.findByDeletedTrue()
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Override
    public UserSummary createAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User admin = new User();
        admin.setFullName(request.getFullName());
        admin.setEmail(request.getEmail());
        admin.setUsername(request.getUsername());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setLocation(request.getLocation());
        admin.setRole(Role.ADMIN);      // always ADMIN, not configurable
        admin.setVerified(true);        // admins don't need verification
        admin.setEnabled(true);
        admin.setOauthProvider(OAuthProvider.LOCAL);

        userRepository.save(admin);

        return toSummary(admin);
    }

    // for pending
    @Override
    public List<PhotographerVerificationResponse> getPendingPhotographers() {
        return userRepository
                .findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.PENDING)
                .stream()
                .map(this::toVerificationResponse)
                .toList();
    }

//    @Override
//    public PhotographerVerificationResponse updateVerificationStatus(Long id, VerificationStatus newStatus) {
//        User user = userRepository.findByIdAndDeletedFalse(id)
//                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
//
//        if (user.getRole() != Role.PHOTOGRAPHER)
//            throw new RuntimeException("User is not a photographer");
//
//        if (user.getVerificationStatus() != VerificationStatus.PENDING)
//            throw new RuntimeException("Photographer is already " + user.getVerificationStatus().name());
//
//        user.setVerificationStatus(newStatus);
//        user.setVerified(newStatus == VerificationStatus.APPROVED);
//        userRepository.save(user);
//
//        return toVerificationResponse(user);
//    }

    // for approve
    @Override
    public PhotographerVerificationResponse approvePhotographer(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() != Role.PHOTOGRAPHER) {
            throw new RuntimeException("User is not a photographer");
        }

        if (user.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new RuntimeException("Photographer is already " +
                    user.getVerificationStatus().name());
        }

        user.setVerified(true);
        user.setVerificationStatus(VerificationStatus.APPROVED);
        userRepository.save(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User admin = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        notificationService.create(
                user,
                admin,
                "VERIFICATION_APPROVED",
                "Congratulations! Your photographer account has been approved.",
                "/dashboard"
        );

        return toVerificationResponse(user);
    }

    // for reject
    @Override
    public PhotographerVerificationResponse rejectPhotographer(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() != Role.PHOTOGRAPHER) {
            throw new RuntimeException("User is not a photographer");
        }

        if (user.getVerificationStatus() == null ||
                user.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new RuntimeException("Photographer is already " +
                    user.getVerificationStatus().name());
        }

        user.setVerified(false);
        user.setVerificationStatus(VerificationStatus.REJECTED);
        userRepository.save(user);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User admin = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        notificationService.create(
                user,
                admin,
                "VERIFICATION_REJECTED",
                "Your photographer verification request has been rejected. Please contact support for more information.",
                "/profile"
        );

        return toVerificationResponse(user);
    }

    @Override
    public List<PhotographerVerificationResponse> getAllPhotographers() {
        return adminRepo
                .findByRoleAndDeletedFalse(Role.PHOTOGRAPHER)
                .stream()
                .map(this::toVerificationResponse)
                .toList();
    }

    @Override
    public PhotographerVerificationResponse getPhotographerById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() != Role.PHOTOGRAPHER) {
            throw new RuntimeException("User is not a photographer");
        }

        return toVerificationResponse(user);
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getActualUsername())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .pinCount(pinRepository.countByUserId(user.getId()))

                .role(user.getRole().name())
                .verified(user.isVerified())
                .verificationStatus(
                        user.getVerificationStatus() != null
                                ? user.getVerificationStatus().name()
                                : null
                )
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .profilePicture(user.getProfilePicture())
                .deleted(user.isDeleted())
                .joinedAt(user.getCreatedAt())
                .build();
    }

    private PhotographerVerificationResponse toVerificationResponse(User user) {
        return PhotographerVerificationResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .profilePicture(user.getProfilePicture())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getActualUsername())
                .portfolioLink(user.getPortfolioLink())
                .pinCount(pinRepository.countByUserId(user.getId()))
                .bio(user.getBio())
                .location(user.getLocation())
                .verificationStatus(user.getVerificationStatus() != null ? user.getVerificationStatus().name() : null)
                .joinedAt(user.getCreatedAt())
                .build();
    }

}
