package com.example.photoGroupe.service.admin;

import com.example.photoGroupe.dto.admin.CreateAdminRequest;
import com.example.photoGroupe.dto.ads.BannerAdResponse;
import com.example.photoGroupe.dto.ads.BoostResponse;
import com.example.photoGroupe.dto.booking.EventTypeBookingResponse;
import com.example.photoGroupe.dto.booking.SpecializationBookingResponse;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerVerificationResponse;
import com.example.photoGroupe.model.VerificationStatus;
import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.PhotographerBoost;
import com.example.photoGroupe.model.booking.BookingStatus;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
    UserSummary getUserById(Long id);
    List<UserSummary> getAllUsers(); // Admin: return user+photographer
    void deleteUser(Long id);
    void hardDeleteUser(Long id);       // permanent, SUPER_ADMIN only
    UserSummary restoreUser(Long id);   // undo soft delete
    List<UserSummary> getDeletedUsers();
    UserSummary createAdmin(CreateAdminRequest request);
    List<PhotographerVerificationResponse> getPendingPhotographers();
//    PhotographerVerificationResponse updateVerificationStatus(Long id, VerificationStatus newStatus);
    PhotographerVerificationResponse approvePhotographer(Long id);
    PhotographerVerificationResponse rejectPhotographer(Long id);
    List<PhotographerVerificationResponse> getAllPhotographers();
    PhotographerVerificationResponse getPhotographerById(Long id);

    Page<BannerAdResponse> getReviewQueue(int page, int size);
    Page<BannerAdResponse> getAllBanners(int page, int size);
    BannerAdResponse approveBanner(Long bannerId);
    BannerAdResponse rejectBanner(Long bannerId, String reason);

    Page<BoostResponse> getAllBoosts(int page, int size);
    BoostResponse revokeBoost(Long boostId, String reason);

    AdRevenueSummary getRevenueSummary();
    record AdRevenueSummary(BigDecimal bannerRevenue, BigDecimal boostRevenue, BigDecimal totalRevenue) {};
    List<EventTypeBookingResponse> getBookingsGroupedByEventType(BookingStatus status);
    EventTypeBookingResponse getBookingsByEventType(String eventTypeName, BookingStatus status);
    SpecializationBookingResponse getBookingsBySpecialization(String specializationName, BookingStatus status);
    BannerAdResponse removeBanner(Long bannerId, String reason);


    UserSummary updateAdminPassword(Long adminId, String newPassword);
    UserSummary revokeAdmin(Long adminId);
    List<UserSummary> getAllAdmins();


}