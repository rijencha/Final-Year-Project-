package com.example.photoGroupe.service.user;

import com.example.photoGroupe.dto.auth.ChangePasswordRequest;
import com.example.photoGroupe.dto.detail.UpdateUserRequest;
import com.example.photoGroupe.dto.detail.UpgradeToPhotographerRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.model.*;
import com.example.photoGroupe.model.rating.PhotographerReview;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.ads.PhotographerBoostRepository;
import com.example.photoGroupe.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PinRepository pinRepository;
    private final PhotographerReviewRepository reviewRepository;
    private final PhotographerSpecializationRepository specializationRepo;
    private final ProfileViewRepository profileViewRepository;
    private final PhotographerBoostRepository boostRepository;
    private final PasswordEncoder passwordEncoder;   // ← add to constructor fields


    private static final double MIN_REVIEWS_FOR_FULL_TRUST = 20.0;

    @Override
    public UserSummary getPublicUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new RuntimeException("This account is not available");
        }

        return toSummary(user);
    }

    @Override
    public List<PhotographerDetail> getAllPhotographers() {
        List<PhotographerDetail> photographers = userRepository
                .findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.APPROVED)
                .stream()
                .map(this::toPhotographerDetail)
                .collect(Collectors.toList());

        double globalAvg = photographers.stream()
                .filter(p -> p.getRatingCount() > 0)
                .mapToDouble(PhotographerDetail::getAverageRating)
                .average()
                .orElse(4.0);

        Map<Long, LocalDateTime> boostEndByPhotographerId = boostRepository
                .findActiveBoosts(LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        PhotographerBoostRepository.ActiveBoostProjection::getPhotographerId,
                        PhotographerBoostRepository.ActiveBoostProjection::getEndAt,
                        (existing, replacement) -> existing.isAfter(replacement) ? existing : replacement
                ));
        Set<Long> boostedIds = boostEndByPhotographerId.keySet();

        Comparator<PhotographerDetail> byScore = Comparator
                .comparingDouble((PhotographerDetail p) -> rankingScore(p, globalAvg))
                .reversed();

        List<PhotographerDetail> boosted = photographers.stream()
                .filter(p -> boostedIds.contains(p.getId()))
                .peek(p -> {
                    p.setPromoted(true);
                    p.setBoostEndAt(boostEndByPhotographerId.get(p.getId()));
                })
                .sorted(byScore)
                .collect(Collectors.toList());

        List<PhotographerDetail> organic = photographers.stream()
                .filter(p -> !boostedIds.contains(p.getId()))
                .sorted(byScore)
                .collect(Collectors.toList());

        boosted.addAll(organic);
        return boosted;
    }

    @Override
    public PhotographerDetail getPhotographerDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Photographer not found with id: " + id));

        if (!user.isEnabled() || !user.isAccountNonLocked()) {
            throw new RuntimeException("This account is not available");
        }

        return toPhotographerDetail(user);
    }

    @Override
    public UserSummary getUserById(Long id) {
        return null;
    }

    @Override
    public List<UserSummary> getAllUsers() {
        return List.of();
    }

    @Override
    public String updateProfilePicture(Long userId, MultipartFile file) throws IOException {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String url = cloudinaryService.uploadProfilePicture(file, userId);
        user.setProfilePicture(url);
        userRepository.save(user);

        return url;
    }

    @Override
    public UserSummary updateUser(Long userId, UpdateUserRequest request, User currentUser) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Only allow users to update their own profile (unless admin)
        if (!currentUser.getId().equals(userId) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied: cannot update another user's profile");
        }

        // Update common fields if provided
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            boolean usernameTaken = userRepository.existsByUsernameAndIdNot(request.getUsername(), userId);
            if (usernameTaken) {
                throw new RuntimeException("Username '" + request.getUsername() + "' is already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getLocation() != null) {
            user.setLocation(request.getLocation());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // Photographer-only field
        if (request.getPortfolioLink() != null) {
            if (user.getRole() == Role.PHOTOGRAPHER) {
                user.setPortfolioLink(request.getPortfolioLink());
            }
            // silently ignore for non-photographers
        }

        User saved = userRepository.save(user);
        return toSummary(saved);
    }

    @Override
    public UserSummary upgradeToPhotographer(Long userId, UpgradeToPhotographerRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.PHOTOGRAPHER) {
            throw new RuntimeException("Already a photographer");
        }

        if (request.getPortfolioLink() == null || request.getPortfolioLink().isBlank()) {
            throw new RuntimeException("Portfolio link is required");
        }

        user.setRole(Role.PHOTOGRAPHER);
        user.setPortfolioLink(request.getPortfolioLink());
        user.setBio(request.getBio());
        user.setVerified(false);
        user.setVerificationStatus(VerificationStatus.PENDING);

        userRepository.save(user);
        return toSummary(user); // already exists in your class
    }

    @Override
    public void updateInterests(Long userId, List<String> interests, Long currentUserId) {
        if (!userId.equals(currentUserId)) throw new AccessDeniedException("Not authorized");
        User user = userRepository.findById(userId).orElseThrow();
        user.setInterests(String.join(",", interests));
        userRepository.save(user);
    }

//    @Override
//    public List<PhotographerDetail> getTopPhotographers(int limit) {
//        List<PhotographerDetail> photographers = userRepository
//                .findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.APPROVED)
//                .stream()
//                .map(this::toPhotographerDetail)
//                .filter(p -> p.getRatingCount() > 0)
//                .collect(Collectors.toList());
//
//        double globalAvg = photographers.stream()
//                .mapToDouble(PhotographerDetail::getAverageRating)
//                .average()
//                .orElse(4.0);
//
//        return photographers.stream()
//                .sorted(Comparator
//                        .comparingDouble((PhotographerDetail p) -> rankingScore(p, globalAvg))
//                        .reversed())
//                .limit(limit)
//                .collect(Collectors.toList());
//    }

    @Override
    public List<PhotographerDetail> getTopPhotographers(int limit) {
        List<PhotographerDetail> photographers = userRepository
                .findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.APPROVED)
                .stream()
                .map(this::toPhotographerDetail)
                .collect(Collectors.toList());

        double globalAvg = photographers.stream()
                .filter(p -> p.getRatingCount() > 0)
                .mapToDouble(PhotographerDetail::getAverageRating)
                .average()
                .orElse(4.0);

        Map<Long, LocalDateTime> boostEndByPhotographerId = boostRepository
                .findActiveBoosts(LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        PhotographerBoostRepository.ActiveBoostProjection::getPhotographerId,
                        PhotographerBoostRepository.ActiveBoostProjection::getEndAt,
                        (existing, replacement) -> existing.isAfter(replacement) ? existing : replacement
                ));
        Set<Long> boostedIds = boostEndByPhotographerId.keySet();

        Comparator<PhotographerDetail> byScore = Comparator
                .comparingDouble((PhotographerDetail p) -> rankingScore(p, globalAvg))
                .reversed();

        // Boosted photographers are featured regardless of rating count — they paid to be seen.
        List<PhotographerDetail> boosted = photographers.stream()
                .filter(p -> boostedIds.contains(p.getId()))
                .peek(p -> {
                    p.setPromoted(true);
                    p.setBoostEndAt(boostEndByPhotographerId.get(p.getId()));
                })
                .sorted(byScore)
                .collect(Collectors.toList());

        // Organic "top" ranking still requires at least one rating, same as before.
        List<PhotographerDetail> organic = photographers.stream()
                .filter(p -> !boostedIds.contains(p.getId()) && p.getRatingCount() > 0)
                .sorted(byScore)
                .collect(Collectors.toList());

        List<PhotographerDetail> combined = new ArrayList<>(boosted);
        combined.addAll(organic);

        return combined.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void recordProfileView(Long profileOwnerId, Long viewerId) {
        if (viewerId == null || viewerId.equals(profileOwnerId)) {
            return;
        }

        LocalDate today = LocalDate.now();
        boolean alreadyViewedToday = profileViewRepository
                .existsByViewerIdAndProfileOwnerIdAndViewedDate(viewerId, profileOwnerId, today);

        if (alreadyViewedToday) {
            return;
        }

        profileViewRepository.save(new ProfileView(viewerId, profileOwnerId, today));

        userRepository.findById(profileOwnerId).ifPresent(owner -> {
            owner.setProfileViewCount(owner.getProfileViewCount() + 1);
            userRepository.save(owner);
        });
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request, User currentUser) {
        // Only allow users to change their own password
        if (!currentUser.getId().equals(userId)) {
            throw new AccessDeniedException("Cannot change another user's password");
        }

        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // OAuth accounts don't have a local password to change
        if (user.getOauthProvider() != OAuthProvider.LOCAL) {
            throw new RuntimeException("Password change is not available for accounts signed in with " + user.getOauthProvider().name());
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new RuntimeException("New password must be at least 8 characters");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("New password and confirmation do not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }


    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .username(user.getActualUsername())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .role(user.getRole().name())
                .verified(user.isVerified())
                .verificationStatus(
                        user.getVerificationStatus() != null
                                ? user.getVerificationStatus().name()
                                : null                              // null for USER/ADMIN roles
                )
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .profilePicture(user.getProfilePicture())
                .deleted(user.isDeleted())
                .joinedAt(user.getCreatedAt())
                .pinCount(pinRepository.countByUserId(user.getId()))
                .profileViewCount(user.getProfileViewCount())
                .build();
    }

    private PhotographerDetail toPhotographerDetail(User user) {
        List<PhotographerReview> reviews =
                reviewRepository.findByPhotographerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

        double avg = reviews.stream()
                .filter(r -> r.getRating() > 0)
                .mapToInt(PhotographerReview::getRating)
                .average()
                .orElse(0.0);
        long ratingCount = reviews.stream()
                .filter(r -> r.getRating() > 0)
                .count();

        long reviewCount = reviews.stream()
                .filter(r -> r.getComment() != null && !r.getComment().isBlank())
                .count();
        List<SpecializationResponse> specializations = specializationRepo  // ← add this
                .findAllByPhotographerId(user.getId())
                .stream()
                .map(SpecializationResponse::from)
                .toList();
        return PhotographerDetail.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getActualUsername())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .location(user.getLocation())
                .profilePicture(user.getProfilePicture())
                .pinCount(pinRepository.countByUserId(user.getId()))
                .verified(user.isVerified())
                .enable(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .deleted(user.isDeleted())
                .joinedAt(user.getCreatedAt())
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .ratingCount(ratingCount)
                .reviewCount(reviewCount)
                .yearsOfExperience(user.getYearsOfExperience())       // ← add this
                .specializations(specializations)
                .profileViewCount(user.getProfileViewCount())
                .build();
    }

    private double rankingScore(PhotographerDetail p, double globalAvg) {
        double v = p.getRatingCount();
        double R = p.getAverageRating();
        double m = MIN_REVIEWS_FOR_FULL_TRUST;
        return (v / (v + m)) * R + (m / (v + m)) * globalAvg;
    }
}
