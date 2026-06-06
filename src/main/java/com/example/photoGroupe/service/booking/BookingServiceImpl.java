package com.example.photoGroupe.service.booking;

import com.example.photoGroupe.dto.booking.BookingRequest;
import com.example.photoGroupe.dto.booking.BookingResponse;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.booking.Booking;
import com.example.photoGroupe.model.booking.BookingStatus;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ── Client: create booking ───────────────────────────────────────────

    public BookingResponse create(User client, BookingRequest req) {
        User photographer = userRepository.findById(req.getPhotographerId())
                .orElseThrow(() -> new RuntimeException("Photographer not found"));

        if (photographer.getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("User is not a photographer");

        // Check for scheduling conflict
        if (bookingRepository.isPhotographerBooked(photographer.getId(), req.getEventDate()))
            throw new RuntimeException("Photographer is already booked on this date");

        Booking booking = Booking.builder()
                .client(client)
                .photographer(photographer)
                .eventTitle(req.getEventTitle())
                .eventType(req.getEventType())
                .eventDate(req.getEventDate())
                .location(req.getLocation())
                .description(req.getDescription())
                .durationHours(req.getDurationHours())
                .price(req.getPrice())
                .specialRequests(req.getSpecialRequests())
                .build();

        Booking saved = bookingRepository.save(booking);

        // Notify photographer
        notificationService.create(
                photographer,
                client,
                "BOOKING_REQUEST",
                client.getFullName() + " requested a booking for \"" + req.getEventTitle() + "\"",
                "/dashboard/bookings/" + saved.getId()
        );

        return new BookingResponse(saved);
    }

    // ── Client: my bookings ──────────────────────────────────────────────

    public Page<BookingResponse> getClientBookings(Long clientId, BookingStatus status, Pageable pageable) {
        if (status != null) {
            return bookingRepository
                    .findByClientIdAndStatusOrderByCreatedAtDesc(clientId, status, pageable)
                    .map(BookingResponse::new);
        }
        return bookingRepository
                .findByClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(BookingResponse::new);
    }

    // ── Client: cancel booking ───────────────────────────────────────────

    public BookingResponse cancel(Long bookingId, User client, String reason) {
        Booking booking = findAndValidateClient(bookingId, client.getId());

        if (booking.getStatus() == BookingStatus.COMPLETED)
            throw new RuntimeException("Cannot cancel a completed booking");
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new RuntimeException("Booking is already cancelled");

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        bookingRepository.save(booking);

        // Notify photographer
        notificationService.create(
                booking.getPhotographer(),
                client,
                "BOOKING_CANCELLED",
                client.getFullName() + " cancelled the booking for \"" + booking.getEventTitle() + "\"",
                "/dashboard/bookings/" + bookingId
        );

        return new BookingResponse(booking);
    }

    // ── Photographer: my bookings ────────────────────────────────────────

    public Page<BookingResponse> getPhotographerBookings(Long photographerId, BookingStatus status, Pageable pageable) {
        if (status != null) {
            return bookingRepository
                    .findByPhotographerIdAndStatusOrderByCreatedAtDesc(photographerId, status, pageable)
                    .map(BookingResponse::new);
        }
        return bookingRepository
                .findByPhotographerIdOrderByCreatedAtDesc(photographerId, pageable)
                .map(BookingResponse::new);
    }

    // ── Photographer: confirm booking ────────────────────────────────────

    public BookingResponse confirm(Long bookingId, User photographer) {
        Booking booking = findAndValidatePhotographer(bookingId, photographer.getId());

        if (booking.getStatus() != BookingStatus.PENDING)
            throw new RuntimeException("Only pending bookings can be confirmed");

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        notificationService.create(
                booking.getClient(),
                photographer,
                "BOOKING_CONFIRMED",
                photographer.getFullName() + " confirmed your booking for \"" + booking.getEventTitle() + "\"",
                "/my-bookings/" + bookingId
        );

        return new BookingResponse(booking);
    }

    // ── Photographer: reject booking ─────────────────────────────────────

    public BookingResponse reject(Long bookingId, User photographer, String reason) {
        Booking booking = findAndValidatePhotographer(bookingId, photographer.getId());

        if (booking.getStatus() != BookingStatus.PENDING)
            throw new RuntimeException("Only pending bookings can be rejected");

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);
        bookingRepository.save(booking);

        notificationService.create(
                booking.getClient(),
                photographer,
                "BOOKING_REJECTED",
                photographer.getFullName() + " declined your booking for \"" + booking.getEventTitle() + "\"",
                "/my-bookings/" + bookingId
        );

        return new BookingResponse(booking);
    }

    // ── Photographer: complete booking ───────────────────────────────────

    public BookingResponse complete(Long bookingId, User photographer) {
        Booking booking = findAndValidatePhotographer(bookingId, photographer.getId());

        if (booking.getStatus() != BookingStatus.CONFIRMED)
            throw new RuntimeException("Only confirmed bookings can be marked complete");

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        notificationService.create(
                booking.getClient(),
                photographer,
                "BOOKING_COMPLETED",
                "Your booking for \"" + booking.getEventTitle() + "\" has been marked complete",
                "/my-bookings/" + bookingId
        );

        return new BookingResponse(booking);
    }

    // ── Photographer: earnings & stats ───────────────────────────────────

    public Map<String, Object> getPhotographerStats(Long photographerId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBookings",  bookingRepository.countByPhotographerId(photographerId));
        stats.put("totalEarnings",  bookingRepository.getTotalEarnings(photographerId));
        stats.put("pending",        bookingRepository.countByPhotographerIdAndStatus(photographerId, BookingStatus.PENDING));
        stats.put("confirmed",      bookingRepository.countByPhotographerIdAndStatus(photographerId, BookingStatus.CONFIRMED));
        stats.put("completed",      bookingRepository.countByPhotographerIdAndStatus(photographerId, BookingStatus.COMPLETED));
        return stats;
    }

    // ── Admin ────────────────────────────────────────────────────────────

    public Page<BookingResponse> getAll(BookingStatus status, Pageable pageable) {
        if (status != null) {
            return bookingRepository
                    .findByStatusOrderByCreatedAtDesc(status, pageable)
                    .map(BookingResponse::new);
        }
        return bookingRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(BookingResponse::new);
    }

    public BookingResponse getById(Long bookingId) {
        return new BookingResponse(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found")));
    }

    // ── Create from accepted bid (called by BidService) ──────────────────

    public Booking createFromBid(Bid bid) {
        Booking booking = Booking.builder()
                .client(bid.getEventRequest().getClient())
                .photographer(bid.getPhotographer())
                .bid(bid)
                .eventTitle(bid.getEventRequest().getTitle())
                .eventType(bid.getEventRequest().getEventType())
                .eventDate(bid.getEventRequest().getEventDate())
                .location(bid.getEventRequest().getLocation())
                .description(bid.getEventRequest().getDescription())
                .price(bid.getPrice())
                .build();

        return bookingRepository.save(booking);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Booking findAndValidateClient(Long bookingId, Long clientId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getClient().getId().equals(clientId))
            throw new RuntimeException("Not authorized");
        return booking;
    }

    private Booking findAndValidatePhotographer(Long bookingId, Long photographerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!booking.getPhotographer().getId().equals(photographerId))
            throw new RuntimeException("Not authorized");
        return booking;
    }
}
