package com.example.photoGroupe.service.booking;

import com.example.photoGroupe.dto.admin.AdminForceCancelRequest;
import com.example.photoGroupe.dto.booking.BookingRequest;
import com.example.photoGroupe.dto.booking.BookingResponse;
import com.example.photoGroupe.dto.booking.PackageRequest;
import com.example.photoGroupe.dto.booking.PackageResponse;
import com.example.photoGroupe.dto.chatiing.SendMessageRequest;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.booking.*;
import com.example.photoGroupe.model.chatting.MessageType;
import com.example.photoGroupe.model.restrict.RestrictionType;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.payment.BookingPackageRepository;
import com.example.photoGroupe.repo.payment.PaymentRepository;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.service.chatting.MessageServiceImpl;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.restrict.UserRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PayoutRepository payoutRepository;
    private final BookingPackageRepository packageRepository;
    private final MessageServiceImpl messageService;
    private final PhotographerPackageService photographerPackageService;
    private final PaymentRepository  paymentRepository;
    private final UserRestrictionService userRestrictionService; // add to constructor

    // ── Client: create booking ───────────────────────────────────────────

    public BookingResponse create(User client, BookingRequest req) {
        User photographer = userRepository.findById(req.getPhotographerId())
                .orElseThrow(() -> new RuntimeException("Photographer not found"));

        if (photographer.getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("User is not a photographer");

        if (client.getBookingRestrictedUntil() != null
                && client.getBookingRestrictedUntil().isAfter(LocalDateTime.now()))
            throw new RuntimeException("You are temporarily restricted from making bookings until "
                    + client.getBookingRestrictedUntil());

        if (photographer.getBookingRestrictedUntil() != null
                && photographer.getBookingRestrictedUntil().isAfter(LocalDateTime.now()))
            throw new RuntimeException("This photographer is temporarily unavailable for new bookings");

        userRestrictionService.assertNotRestricted(photographer.getId(), client.getId(), RestrictionType.BOOKING);

        // Check for scheduling conflict
        if (bookingRepository.isPhotographerBooked(photographer.getId(), req.getEventDate()))
            throw new RuntimeException("Photographer is already booked on this date");

        if (req.getEventType() == null && (req.getCustomEventType() == null || req.getCustomEventType().isBlank()))
            throw new RuntimeException("Provide either eventType or customEventType");

        Booking booking = Booking.builder()
                .client(client)
                .photographer(photographer)
                .eventTitle(req.getEventTitle())
                .eventType(req.getEventType())
                .customEventType(req.getCustomEventType())
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
        Page<BookingResponse> page;
        if (status != null) {
            page = bookingRepository
                    .findByClientIdAndStatusOrderByCreatedAtDesc(clientId, status, pageable)
                    .map(BookingResponse::new);
        } else {
            page = bookingRepository
                    .findByClientIdOrderByCreatedAtDesc(clientId, pageable)
                    .map(BookingResponse::new);
        }

        // Stamp hasPendingPayment on each confirmed+unpaid booking
        page.forEach(br -> {
            if (br.getStatus() == BookingStatus.CONFIRMED
                    && br.getPaymentStatus() == PaymentStatus.UNPAID) {
                boolean pending = !paymentRepository
                        .findAllByBookingIdAndStatus(br.getId(), "PENDING")
                        .isEmpty();
                br.setHasPendingPayment(pending);
            }
        });

        return page;
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
        if (booking.getEscrowStatus() != EscrowStatus.HELD)
            throw new RuntimeException("Payment must be held in escrow before completing");

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        // Notify client — they now see the payment release screen
        notificationService.create(
                booking.getClient(),
                photographer,
                "BOOKING_COMPLETED",
                "Your session for \"" + booking.getEventTitle()
                        + "\" is complete — please confirm to release payment",
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
                .customEventType(bid.getEventRequest().getCustomEventType())
                .eventDate(bid.getEventRequest().getEventDate())
                .location(bid.getEventRequest().getLocation())
                .description(bid.getEventRequest().getDescription())
                .price(bid.getPrice())
                .status(BookingStatus.CONFIRMED)   // ← auto-confirm, bid acceptance = agreement
                .build();

        Booking saved = bookingRepository.save(booking);

        // Notify both parties
        notificationService.create(
                bid.getPhotographer(),
                bid.getEventRequest().getClient(),
                "BOOKING_CONFIRMED",
                "A booking has been created for \"" + bid.getEventRequest().getTitle() + "\"",
                "/dashboard/bookings/" + saved.getId()
        );

        notificationService.create(
                bid.getEventRequest().getClient(),
                bid.getPhotographer(),
                "BOOKING_CONFIRMED",
                "Your booking for \"" + bid.getEventRequest().getTitle() + "\" is confirmed — proceed to payment",
                "/my-bookings/" + saved.getId()
        );

        return saved;
    }

    public BookingResponse releasePayment(Long bookingId, User client) {
        Booking booking = findAndValidateClient(bookingId, client.getId());

        if (booking.getStatus() != BookingStatus.COMPLETED)
            throw new RuntimeException("Session must be marked complete first");
        if (booking.getEscrowStatus() != EscrowStatus.HELD)
            throw new RuntimeException("Funds are not currently held in escrow");

        // Calculate the split
        BigDecimal total        = booking.getPrice();
        BigDecimal commission   = total.multiply(BigDecimal.valueOf(0.12))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal photographerPayout = total.subtract(commission);

        // Record the payout
        Payout payout = new Payout();
        payout.setBooking(booking);
        payout.setPhotographer(booking.getPhotographer());
        payout.setTotalAmount(total);
        payout.setCommissionAmount(commission);
        payout.setPhotographerAmount(photographerPayout);
        payout.setStatus("RELEASED");
        payout.setSourceType("BOOKING");
        payout.setReleasedAt(LocalDateTime.now());
        payoutRepository.save(payout);

        // Update booking
        booking.setEscrowStatus(EscrowStatus.RELEASE);
        bookingRepository.save(booking);

        // Notify photographer of their payout
        notificationService.create(
                booking.getPhotographer(),
                client,
                "PAYMENT_RELEASED",
                "Payment released for \"" + booking.getEventTitle()
                        + "\" — you receive NPR " + photographerPayout
                        + " (platform fee: NPR " + commission + ")",
                "/dashboard/earnings"
        );

        return new BookingResponse(booking);
    }

    public PackageResponse sendPackage(PackageRequest req, User photographer) {
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");

        if (booking.getStatus() != BookingStatus.CONFIRMED)
            throw new RuntimeException("Booking must be confirmed before sending a package");

        // Load template — must belong to this photographer
        PhotographerPackage template = photographerPackageService.findById(req.getTemplateId());
        if (!template.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Template does not belong to you");

        boolean alreadySentSameTemplate = packageRepository
                .findAllByBookingId(booking.getId())
                .stream()
                .anyMatch(p -> p.getTemplate() != null
                        && p.getTemplate().getId().equals(template.getId())
                        && p.getStatus() != PackageStatus.REJECTED);

        if (alreadySentSameTemplate)
            throw new RuntimeException("This package template was already sent for this booking");

        // Snapshot template values into the booking instance
        BookingPackage pkg = BookingPackage.builder()
                .booking(booking)
                .template(template)
                .name(template.getName())
                .price(template.getPrice())
                .packageType(template.getPackageType())
                .description(template.getDescription())
                .deliveryDays(template.getDeliveryDays())
                .status(PackageStatus.SENT)
                .negotiationRound(1)
                .lastActionBy("PHOTOGRAPHER")
                .build();

        packageRepository.save(pkg);

        // ── Auto-send as chat message ─────────────────────────────────────
        String messageText = template.getPackageType() == PackageType.PREMIUM
                ? buildPremiumMessage(pkg, booking)
                : buildCustomMessage(pkg, booking);

        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(booking.getClient().getId());
        msgReq.setText(messageText);
        msgReq.setType(MessageType.PACKAGE);
        msgReq.setBookingPackageId(pkg.getId());

        messageService.sendMessage(msgReq, photographer.getId());

        // ── Notify client ─────────────────────────────────────────────────
        notificationService.create(
                booking.getClient(),
                photographer,
                "PACKAGE_SENT",
                photographer.getFullName() + " sent a package for \""
                        + booking.getEventTitle() + "\" — NPR " + template.getPrice(),
                "/my-bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    // ── Update package (photographer only, only if still SENT) ───────────

// ── Delete package (photographer only, only if SENT or REJECTED) ─────

    public void deletePackage(Long packageId, User photographer) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        if (!pkg.getBooking().getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");

        if (pkg.getStatus() == PackageStatus.ACCEPTED)
            throw new RuntimeException("Cannot delete an accepted package");

        packageRepository.delete(pkg);
    }

    // ── Admin: force cancel + optional penalty ────────────────────────────

    public BookingResponse adminForceCancel(Long bookingId, User admin, AdminForceCancelRequest req) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() == BookingStatus.COMPLETED)
            throw new RuntimeException("Cannot force cancel a completed booking");
        if (booking.getStatus() == BookingStatus.CANCELLED)
            throw new RuntimeException("Booking is already cancelled");

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationReason(
                req.getReason() != null && !req.getReason().isBlank()
                        ? req.getReason()
                        : "Cancelled by admin"
        );
        bookingRepository.save(booking);

        int penaltyDays = (req.getPenaltyDays() != null && req.getPenaltyDays() > 0) ? req.getPenaltyDays() : 10;
        String party = req.getPenalizeParty() != null ? req.getPenalizeParty().toUpperCase() : "NONE";

        if (party.equals("CLIENT") || party.equals("BOTH")) {
            applyBookingPenalty(booking.getClient(), penaltyDays, admin, booking);
        }
        if (party.equals("PHOTOGRAPHER") || party.equals("BOTH")) {
            applyBookingPenalty(booking.getPhotographer(), penaltyDays, admin, booking);
        }

        notificationService.create(
                booking.getClient(),
                admin,
                "BOOKING_ADMIN_CANCELLED",
                "Your booking for \"" + booking.getEventTitle() + "\" was cancelled by an admin"
                        + (req.getReason() != null && !req.getReason().isBlank() ? ": " + req.getReason() : ""),
                "/my-bookings/" + bookingId
        );
        notificationService.create(
                booking.getPhotographer(),
                admin,
                "BOOKING_ADMIN_CANCELLED",
                "The booking for \"" + booking.getEventTitle() + "\" was cancelled by an admin"
                        + (req.getReason() != null && !req.getReason().isBlank() ? ": " + req.getReason() : ""),
                "/dashboard/bookings/" + bookingId
        );

        return new BookingResponse(booking);
    }

// ── Get all packages for a photographer ──────────────────────────────

    public List<PackageResponse> getPhotographerPackages(User photographer) {
        return packageRepository.findAllByBookingPhotographerId(photographer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Client accepts package ───────────────────────────────────────────

    public PackageResponse acceptPackage(Long packageId, User client) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Booking booking = pkg.getBooking();

        if (!booking.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Not authorized");

        if (pkg.getPackageType() == PackageType.PREMIUM) {
            if (pkg.getStatus() != PackageStatus.SENT)
                throw new RuntimeException("Package already responded to");
        } else {
            if (pkg.getStatus() != PackageStatus.SENT && pkg.getStatus() != PackageStatus.COUNTERED)
                throw new RuntimeException("Package already responded to");
        }

        pkg.setStatus(PackageStatus.ACCEPTED);
        packageRepository.save(pkg);

        BigDecimal finalPrice = (pkg.getPackageType() == PackageType.CUSTOM
                && pkg.getCounterPrice() != null)
                ? pkg.getCounterPrice()
                : pkg.getPrice();

        pkg.setPrice(finalPrice);
        pkg.setStatus(PackageStatus.ACCEPTED);
        packageRepository.save(pkg);

        booking.setPrice(finalPrice);
        bookingRepository.save(booking);

        // ── Notify photographer ───────────────────────────────────────────
        notificationService.create(
                booking.getPhotographer(),
                client,
                "PACKAGE_ACCEPTED",
                client.getFullName() + " accepted your package for \""
                        + booking.getEventTitle() + "\"",
                "/dashboard/bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    // ── Client rejects package ───────────────────────────────────────────

    public PackageResponse rejectPackage(Long packageId, User client) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Booking booking = pkg.getBooking();

        if (!booking.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Not authorized");

        pkg.setStatus(PackageStatus.REJECTED);
        packageRepository.save(pkg);

        notificationService.create(
                booking.getPhotographer(),
                client,
                "PACKAGE_REJECTED",
                client.getFullName() + " rejected your package for \""
                        + booking.getEventTitle() + "\"",
                "/dashboard/bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    public PackageResponse counterOffer(Long packageId, BigDecimal counterPrice, User client) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Booking booking = pkg.getBooking();

        if (!booking.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Not authorized");

        if (pkg.getPackageType() != PackageType.CUSTOM)
            throw new RuntimeException("Counter-offers only allowed on CUSTOM packages");

        if (pkg.getStatus() == PackageStatus.ACCEPTED
                || pkg.getStatus() == PackageStatus.REJECTED)
            throw new RuntimeException("Negotiation is already closed");

        pkg.setCounterPrice(counterPrice);
        pkg.setStatus(PackageStatus.COUNTERED);
        pkg.setNegotiationRound(pkg.getNegotiationRound() + 1);
        pkg.setLastActionBy("CLIENT");
        packageRepository.save(pkg);

        // Auto-send counter as message
        String text = "💬 Counter Offer (Round " + pkg.getNegotiationRound() + ")\n"
                + "Proposed price: NPR " + counterPrice.toPlainString()
                + "\nFor: " + booking.getEventTitle();

        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(booking.getPhotographer().getId());
        msgReq.setText(text);
        msgReq.setType(MessageType.PACKAGE);
        msgReq.setBookingPackageId(pkg.getId());
        messageService.sendMessage(msgReq, client.getId());

        notificationService.create(
                booking.getPhotographer(),
                client,
                "PACKAGE_COUNTERED",
                client.getFullName() + " sent a counter offer for \""
                        + booking.getEventTitle() + "\" — NPR " + counterPrice,
                "/dashboard/bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    public PackageResponse acceptCounter(Long packageId, User photographer) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Booking booking = pkg.getBooking();

        if (!booking.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");

        if (pkg.getStatus() != PackageStatus.COUNTERED)
            throw new RuntimeException("No counter offer to accept");

        // Lock in the countered price
        pkg.setPrice(pkg.getCounterPrice());
        pkg.setStatus(PackageStatus.ACCEPTED);
        pkg.setLastActionBy("PHOTOGRAPHER");
        packageRepository.save(pkg);

        // Sync to booking for payment
        booking.setPrice(pkg.getPrice());
        bookingRepository.save(booking);

        notificationService.create(
                booking.getClient(),
                photographer,
                "PACKAGE_ACCEPTED",
                photographer.getFullName() + " accepted your counter offer — NPR "
                        + pkg.getPrice() + " is now confirmed",
                "/my-bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    public PackageResponse reCounter(Long packageId, BigDecimal newPrice, User photographer) {
        BookingPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found"));

        Booking booking = pkg.getBooking();

        if (!booking.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");

        if (pkg.getPackageType() != PackageType.CUSTOM)
            throw new RuntimeException("Only CUSTOM packages support negotiation");

        // Update the base price to the new proposal
        pkg.setPrice(newPrice);
        pkg.setCounterPrice(null);
        pkg.setStatus(PackageStatus.SENT);   // back to sent — ball in client's court
        pkg.setNegotiationRound(pkg.getNegotiationRound() + 1);
        pkg.setLastActionBy("PHOTOGRAPHER");
        packageRepository.save(pkg);

        String text = "🔄 Revised Offer (Round " + pkg.getNegotiationRound() + ")\n"
                + "New proposed price: NPR " + newPrice.toPlainString()
                + "\nFor: " + booking.getEventTitle();

        SendMessageRequest msgReq = new SendMessageRequest();
        msgReq.setReceiverId(booking.getClient().getId());
        msgReq.setText(text);
        msgReq.setType(MessageType.PACKAGE);
        msgReq.setBookingPackageId(pkg.getId());
        messageService.sendMessage(msgReq, photographer.getId());

        notificationService.create(
                booking.getClient(),
                photographer,
                "PACKAGE_REVISED",
                photographer.getFullName() + " revised the offer for \""
                        + booking.getEventTitle() + "\" — NPR " + newPrice,
                "/my-bookings/" + booking.getId()
        );

        return toResponse(pkg);
    }

    // ── Get package by booking ───────────────────────────────────────────

    public PackageResponse getByBookingId(Long bookingId) {
        return packageRepository.findAllByBookingId(bookingId)
                .stream()
                .max(Comparator.comparing(BookingPackage::getCreatedAt))
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("No package for this booking"));

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

    private String buildPremiumMessage(BookingPackage pkg, Booking booking) {
        return """
            ⭐ Premium Package — %s

            Price    : NPR %s
            Delivery : %d days after the event

            Includes:
            %s

            This is a fixed-price package. Accept or reject from your bookings page.
            """.formatted(
                booking.getEventTitle(),
                pkg.getPrice().toPlainString(),
                pkg.getDeliveryDays(),
                pkg.getDescription()
        );
    }

    private String buildCustomMessage(BookingPackage pkg, Booking booking) {
        return """
            🤝 Custom Package Proposal — %s

            Initial Price : NPR %s
            Delivery      : %d days after the event

            Details:
            %s

            This is open to negotiation. You can counter-offer from your bookings page.
            """.formatted(
                booking.getEventTitle(),
                pkg.getPrice().toPlainString(),
                pkg.getDeliveryDays(),
                pkg.getDescription()
        );
    }

    private PackageResponse toResponse(BookingPackage pkg) {
        return PackageResponse.builder()
                .id(pkg.getId())
                .bookingId(pkg.getBooking().getId())
                .name(pkg.getName())
                .price(pkg.getPrice())
                .counterPrice(pkg.getCounterPrice())
                .packageType(pkg.getPackageType())
                .description(pkg.getDescription())
                .deliveryDays(pkg.getDeliveryDays())
                .status(pkg.getStatus())
                .negotiationRound(pkg.getNegotiationRound())
                .lastActionBy(pkg.getLastActionBy())
                .createdAt(pkg.getCreatedAt())
                .build();
    }

    private void applyBookingPenalty(User user, int days, User admin, Booking booking) {
        LocalDateTime restrictedUntil = LocalDateTime.now().plusDays(days);
        user.setBookingRestrictedUntil(restrictedUntil);
        userRepository.save(user);

        notificationService.create(
                user,
                admin,
                "BOOKING_PENALTY",
                "You've been restricted from making or receiving bookings for " + days
                        + " days, following the cancellation of \"" + booking.getEventTitle() + "\" by an admin.",
                "/support"
        );
    }
}
