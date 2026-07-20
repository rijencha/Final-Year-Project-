package com.example.photoGroupe.service.booking;

import com.example.photoGroupe.dto.admin.AdminForceCancelRequest;
import com.example.photoGroupe.dto.booking.BookingRequest;
import com.example.photoGroupe.dto.booking.BookingResponse;
import com.example.photoGroupe.dto.booking.PackageRequest;
import com.example.photoGroupe.dto.booking.PackageResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

// service/booking/BookingService.java
public interface BookingService {

    // ── Client ───────────────────────────────────────────────────────────────
    BookingResponse create(User client, BookingRequest req);
    Page<BookingResponse> getClientBookings(Long clientId, BookingStatus status, Pageable pageable);
    BookingResponse cancel(Long bookingId, User client, String reason);

    // ── Photographer ─────────────────────────────────────────────────────────
    Page<BookingResponse> getPhotographerBookings(Long photographerId, BookingStatus status, Pageable pageable);
    BookingResponse confirm(Long bookingId, User photographer);
    BookingResponse reject(Long bookingId, User photographer, String reason);
    BookingResponse complete(Long bookingId, User photographer);
    Map<String, Object> getPhotographerStats(Long photographerId);

    // ── Admin ─────────────────────────────────────────────────────────────────
    Page<BookingResponse> getAll(BookingStatus status, Pageable pageable);
    BookingResponse getById(Long bookingId);

    // ── Internal (called by BidService) ──────────────────────────────────────
    Booking createFromBid(Bid bid);
    BookingResponse releasePayment(Long bookingId, User client);

    // create package
    PackageResponse sendPackage(PackageRequest req, User photographer);
    PackageResponse acceptPackage(Long packageId, User client);
    PackageResponse rejectPackage(Long packageId, User client);
    PackageResponse getByBookingId(Long bookingId);
    List<PackageResponse> getPhotographerPackages(User photographer);
    void deletePackage(Long packageId, User photographer);

    BookingResponse adminForceCancel(Long bookingId, User admin, AdminForceCancelRequest req);
}
