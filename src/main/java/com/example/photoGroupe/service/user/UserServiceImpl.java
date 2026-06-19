package com.example.photoGroupe.service.user;

import com.example.photoGroupe.dto.detail.UpdateUserRequest;
import com.example.photoGroupe.dto.detail.UpgradeToPhotographerRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.rating.ReviewResponse;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.model.rating.PhotographerReview;
import com.example.photoGroupe.repo.PhotographerReviewRepository;
import com.example.photoGroupe.repo.PhotographerSpecializationRepository;
import com.example.photoGroupe.repo.PinRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PinRepository pinRepository;
    private final PhotographerReviewRepository reviewRepository;
    private final PhotographerSpecializationRepository specializationRepo;

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
        return userRepository.findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.APPROVED)
                .stream()
                .map(this::toPhotographerDetail)
                .collect(Collectors.toList());
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

    @Override
    public List<PhotographerDetail> getTopPhotographers(int limit) {
        return userRepository
                .findByRoleAndVerificationStatusAndDeletedFalse(Role.PHOTOGRAPHER, VerificationStatus.APPROVED)
                .stream()
                .map(this::toPhotographerDetail)
                .filter(p -> p.getRatingCount() > 0)          // only rated photographers
                .sorted(Comparator
                        .comparingDouble(PhotographerDetail::getAverageRating).reversed()
                        .thenComparingLong(PhotographerDetail::getReviewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
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
                .build();
    }

//    private PhotographerDetail toPhotographerDetail(User user) {
//        return PhotographerDetail.builder()
//                .id(user.getId())
//                .fullName(user.getFullName())
//                .username(user.getActualUsername())
//                .phoneNumber(user.getPhoneNumber())
//                .bio(user.getBio())
//                .location(user.getLocation())
//                .profilePicture(user.getProfilePicture())
//                .pinCount(pinRepository.countByUserId(user.getId()))
//                .verified(user.isVerified())
//                .enable(user.isEnabled())
//                .accountNonLocked(user.isAccountNonLocked())
//                .deleted(user.isDeleted())
//                .joinedAt(user.getCreatedAt())
//                .build();
//    }
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
    //            .recentReviews(reviewResponses)
                .yearsOfExperience(user.getYearsOfExperience())       // ← add this
                .specializations(specializations)
                .build();
    }
}
