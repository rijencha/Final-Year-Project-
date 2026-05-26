package com.example.photoGroupe.service.user;

import com.example.photoGroupe.dto.detail.UpdateUserRequest;
import com.example.photoGroupe.dto.detail.UpgradeToPhotographerRequest;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

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

        if (user.getRole() != Role.PHOTOGRAPHER) {
            throw new RuntimeException("User is not a photographer");
        }

        if (!user.isVerified()) {
            throw new RuntimeException("Photographer is not verified");
        }

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

    private UserSummary toSummary(User user) {
        return UserSummary.builder()
                .id(user.getId())
                .email(user.getEmail())
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
                .build();
    }

    private PhotographerDetail toPhotographerDetail(User user) {
        return PhotographerDetail.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getActualUsername())
                .bio(user.getBio())
                .location(user.getLocation())
                .profilePicture(user.getProfilePicture())
                .verified(user.isVerified())
                .enable(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .deleted(user.isDeleted())
                .build();
    }
}
