package com.example.photoGroupe.service.admin;

import com.example.photoGroupe.dto.admin.CreateAdminRequest;
import com.example.photoGroupe.dto.ads.BannerAdResponse;
import com.example.photoGroupe.dto.ads.BoostResponse;
import com.example.photoGroupe.dto.booking.BookingResponse;
import com.example.photoGroupe.dto.booking.EventTypeBookingResponse;
import com.example.photoGroupe.dto.booking.SpecializationBookingResponse;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerVerificationResponse;
import com.example.photoGroupe.model.OAuthProvider;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.model.ads.PhotographerBoost;
import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.model.rating.PhotographerReview;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.ads.BannerAdRepository;
import com.example.photoGroupe.repo.ads.PhotographerBoostRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.album.AlbumService;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AdminRepo adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final PinRepository pinRepository;
    private final NotificationService notificationService;
    private final PhotographerReviewRepository reviewRepository;
    private final BannerAdRepository bannerAdRepository;
    private final PhotographerBoostRepository boostRepository;
    private final BookingRepository bookingRepository;
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

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User requester = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        if (user.getRole() == Role.ADMIN && requester.getRole() != Role.SUPER_ADMIN)
            throw new RuntimeException("Only a super admin can delete an admin account");

        if (user.getRole() == Role.SUPER_ADMIN)
            throw new RuntimeException("Super admin accounts cannot be deleted");

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

    @Override
    @Transactional(readOnly = true)
    public Page<BannerAdResponse> getReviewQueue(int page, int size) {
        return bannerAdRepository.findByStatusOrderByCreatedAtAsc(
                BannerStatus.PENDING_REVIEW, PageRequest.of(page, size)
        ).map(BannerAdResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BannerAdResponse> getAllBanners(int page, int size) {
        return bannerAdRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(BannerAdResponse::from);
    }

    @Override
    @Transactional
    public BannerAdResponse approveBanner(Long bannerId) {
        BannerAd banner = bannerAdRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        if (banner.getStatus() != BannerStatus.PENDING_REVIEW)
            throw new RuntimeException("Only banners pending review can be approved");

        LocalDateTime now = LocalDateTime.now();
        banner.setStatus(BannerStatus.ACTIVE);
        banner.setStartAt(now);
        banner.setEndAt(now.plusDays(banner.getDaysPurchased()));
        bannerAdRepository.save(banner);

        notificationService.create(
                banner.getAdvertiser(), banner.getAdvertiser(), "BANNER_ACTIVE",
                "Your banner \"" + banner.getTitle() + "\" was approved and is now live for "
                        + banner.getDaysPurchased() + " day(s)",
                "/dashboard/ads/" + banner.getId()
        );

        return BannerAdResponse.from(banner);
    }

    @Override
    @Transactional
    public BannerAdResponse rejectBanner(Long bannerId, String reason) {
        BannerAd banner = bannerAdRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        if (banner.getStatus() != BannerStatus.PENDING_REVIEW)
            throw new RuntimeException("Only banners pending review can be rejected");

        banner.setStatus(BannerStatus.REJECTED);
        bannerAdRepository.save(banner);

        notificationService.create(
                banner.getAdvertiser(), banner.getAdvertiser(), "BANNER_REJECTED",
                "Your banner \"" + banner.getTitle() + "\" was rejected"
                        + (reason != null && !reason.isBlank() ? ": " + reason : "")
                        + ". Contact support for a refund.",
                "/dashboard/ads/" + banner.getId()
        );

        return BannerAdResponse.from(banner);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BoostResponse> getAllBoosts(int page, int size) {
        return boostRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(BoostResponse::from);
    }

    @Override
    @Transactional
    public BoostResponse revokeBoost(Long boostId, String reason) {
        PhotographerBoost boost = boostRepository.findById(boostId)
                .orElseThrow(() -> new RuntimeException("Boost not found"));

        if (boost.getStatus() != BannerStatus.ACTIVE)
            throw new RuntimeException("Only active boosts can be revoked");

        boost.setStatus(BannerStatus.CANCELLED);
        boost.setEndAt(LocalDateTime.now());
        boostRepository.save(boost);

        notificationService.create(
                boost.getPhotographer(), boost.getPhotographer(), "BOOST_REVOKED",
                "Your featured placement was revoked by an admin"
                        + (reason != null && !reason.isBlank() ? ": " + reason : ""),
                "/dashboard/boost"
        );

        return BoostResponse.from(boost);
    }

    // ─── Revenue Summary ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AdRevenueSummary getRevenueSummary() {
        var bannerRevenue = bannerAdRepository.sumRevenue();
        var boostRevenue = boostRepository.sumRevenue();
        return new AdRevenueSummary(bannerRevenue, boostRevenue, bannerRevenue.add(boostRevenue));
    }
    //----------------------------------------------------------------------------------------------

    public List<EventTypeBookingResponse> getBookingsGroupedByEventType(BookingStatus status) {
        List<Booking> allBookings = bookingRepository.findAllForEventTypeGrouping(status);

        Map<String, List<Booking>> grouped = new LinkedHashMap<>();
        for (Booking b : allBookings) {
            String typeName = resolveEventTypeName(b);
            grouped.computeIfAbsent(typeName, k -> new ArrayList<>()).add(b);
        }

        List<EventTypeBookingResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Booking>> entry : grouped.entrySet()) {
            List<Booking> bookings = entry.getValue();

            BigDecimal total = bookings.stream()
                    .map(Booking::getPrice)
                    .filter(p -> p != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(EventTypeBookingResponse.builder()
                    .eventTypeName(entry.getKey())
                    .bookingCount(bookings.size())
                    .totalRevenue(total)
                    .bookings(bookings.stream().map(BookingResponse::new).toList())
                    .build());
        }

        return result;
    }

    public EventTypeBookingResponse getBookingsByEventType(String eventTypeName, BookingStatus status) {
        List<Booking> allBookings = bookingRepository.findAllForEventTypeGrouping(status);

        List<Booking> matched = allBookings.stream()
                .filter(b -> resolveEventTypeName(b).equalsIgnoreCase(eventTypeName))
                .toList();

        BigDecimal total = matched.stream()
                .map(Booking::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return EventTypeBookingResponse.builder()
                .eventTypeName(eventTypeName)
                .bookingCount(matched.size())
                .totalRevenue(total)
                .bookings(matched.stream().map(BookingResponse::new).toList())
                .build();
    }

    /**
     * Bookings for a single specialization (e.g. "Wedding" or a custom type like "Drone Photography").
     */
    public SpecializationBookingResponse getBookingsBySpecialization(String specializationName, BookingStatus status) {
        List<Booking> bookings = bookingRepository.findBookingsBySpecializationName(specializationName, status);

        BigDecimal total = bookings.stream()
                .map(Booking::getPrice)
                .filter(p -> p != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SpecializationBookingResponse.builder()
                .specializationName(specializationName)
                .bookingCount(bookings.size())
                .totalRevenue(total)
                .bookings(bookings.stream().map(BookingResponse::new).toList())
                .build();
    }

    @Override
    @Transactional
    public BannerAdResponse removeBanner(Long bannerId, String reason) {
        BannerAd banner = bannerAdRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        if (banner.getStatus() != BannerStatus.ACTIVE)
            throw new RuntimeException("Only active banners can be removed");

        banner.setStatus(BannerStatus.CANCELLED);
        banner.setEndAt(LocalDateTime.now());
        bannerAdRepository.save(banner);

        notificationService.create(
                banner.getAdvertiser(), banner.getAdvertiser(), "BANNER_REMOVED",
                "Your banner \"" + banner.getTitle() + "\" was removed by an admin"
                        + (reason != null && !reason.isBlank() ? ": " + reason : "")
                        + ". Contact support if you have questions.",
                "/dashboard/ads/" + banner.getId()
        );

        return BannerAdResponse.from(banner);
    }

    // ─── Update admin password (SUPER_ADMIN only) ───────────────────────

    @Override
    public UserSummary updateAdminPassword(Long adminId, String newPassword) {
        User admin = userRepository.findByIdAndDeletedFalse(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (admin.getRole() != Role.ADMIN)
            throw new RuntimeException("Target user is not an admin");

        if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8)
            throw new RuntimeException("Password must be at least 8 characters");

        admin.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(admin);

        return toSummary(admin);
    }

    // ─── Revoke admin privileges (demote ADMIN → USER, SUPER_ADMIN only) ─

    @Override
    public UserSummary revokeAdmin(Long adminId) {
        User admin = userRepository.findByIdAndDeletedFalse(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));

        if (admin.getRole() != Role.ADMIN)
            throw new RuntimeException("Target user is not an admin");

//        admin.setRole(Role.USER);
//        userRepository.save(admin);
        admin.setRole(Role.USER);
        admin.setVerified(false);   // optional — reset since verified has no meaning for a plain USER
        userRepository.save(admin);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User superAdmin = ((CustomUserDetails) authentication.getPrincipal()).getUser();

        notificationService.create(
                admin,
                superAdmin,
                "ADMIN_REVOKED",
                "Your admin privileges have been revoked.",
                "/dashboard"
        );

        return toSummary(admin);
    }

    @Override
    public List<UserSummary> getAllAdmins() {
        List<User> admins = userRepository.findByRoleInAndDeletedFalse(
                List.of(Role.ADMIN, Role.SUPER_ADMIN)
        );

        return admins.stream()
                .map(this::toSummary)
                .toList();
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private String resolveEventTypeName(Booking b) {
        if (b.getEventType() != null) return b.getEventType().name();
        if (b.getCustomEventType() != null && !b.getCustomEventType().isBlank())
            return b.getCustomEventType().trim();
        return "Uncategorized";
    }

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
                .deletedAt(user.getDeletedAt())
                .joinedAt(user.getCreatedAt())
                .build();
    }

    private PhotographerVerificationResponse toVerificationResponse(User user) {
        List<PhotographerReview> reviews =
                reviewRepository.findByPhotographerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

        double avg = reviews.stream()
                .mapToInt(PhotographerReview::getRating)
                .average()
                .orElse(0.0);

        long ratingCount = reviews.stream()
                .filter(r -> r.getRating() > 0)
                .count();

        long reviewCount = reviews.stream()
                .filter(r -> r.getComment() != null && !r.getComment().isBlank())
                .count();

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
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .ratingCount(ratingCount)
                .reviewCount(reviewCount)
                .build();
    }

}
