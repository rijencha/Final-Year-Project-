package com.example.photoGroupe.service.payment;

import com.example.photoGroupe.config.EsewaConfig;
import com.example.photoGroupe.dto.payment.EsewaFormData;
import com.example.photoGroupe.dto.payment.EsewaPaymentRequest;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.ads.BannerAd;
import com.example.photoGroupe.model.ads.BannerStatus;
import com.example.photoGroupe.model.ads.PhotographerBoost;
import com.example.photoGroupe.model.booking.*;
import com.example.photoGroupe.model.workshop.Workshop;
import com.example.photoGroupe.model.workshop.WorkshopParticipant;
import com.example.photoGroupe.model.workshop.WorkshopParticipantStatus;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.ads.BannerAdRepository;
import com.example.photoGroupe.repo.ads.PhotographerBoostRepository;
import com.example.photoGroupe.repo.payment.BookingPackageRepository;
import com.example.photoGroupe.repo.payment.PaymentRepository;
import com.example.photoGroupe.repo.payment.PayoutRepository;
import com.example.photoGroupe.repo.workshop.WorkshopParticipantRepository;
import com.example.photoGroupe.repo.workshop.WorkshopRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.ads.AdPricingConfig;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.util.EsewaSignatureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EsewaPaymentService {

    private final EsewaConfig esewaConfig;
    private final EsewaSignatureUtil signatureUtil;
    private final PaymentRepository paymentRepository;
    private final PayoutRepository  payoutRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final BookingPackageRepository packageRepository;
    private final WorkshopRepository workshopRepository;
    private final WorkshopParticipantRepository participantRepository;
    private final BannerAdRepository bannerAdRepository;
    private final PhotographerBoostRepository boostRepository;
    private final AdPricingConfig adPricingConfig;

    private static final BigDecimal WORKSHOP_COMMISSION_RATE = new BigDecimal("0.12"); // 12%
    private static final long PAYMENT_GRACE_PERIOD_MINUTES = 10;


    public EsewaFormData initiatePayment(EsewaPaymentRequest request,
                                         CustomUserDetails currentUser,
                                         Long bookingId) throws Exception {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // ✅ make sure this is the client's own booking
        if (!booking.getClient().getId().equals(currentUser.getUser().getId()))
            throw new RuntimeException("Not authorized for this booking");

        // ✅ only pay for confirmed bookings
        if (booking.getStatus() != BookingStatus.CONFIRMED)
            throw new RuntimeException("Booking must be confirmed before payment");

        // ✅ prevent duplicate payments
        if (booking.getEscrowStatus() == EscrowStatus.HELD
                || booking.getEscrowStatus() == EscrowStatus.RELEASE)
            throw new RuntimeException("This booking has already been paid for");

        boolean hasPendingPackage = packageRepository
                .findAllByBookingId(bookingId)
                .stream()
                .anyMatch(p -> p.getStatus() == PackageStatus.SENT
                        || p.getStatus() == PackageStatus.COUNTERED);

        if (hasPendingPackage)
            throw new RuntimeException("Please respond to the pending package offer before paying");

        String transactionUuid = UUID.randomUUID().toString();

        // ✅ use the price decided at booking time (incl. any package negotiation),
        //    NOT whatever the client sends
        String totalAmount = booking.getPrice()
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        String signature = signatureUtil.generateSignature(
                totalAmount, transactionUuid,
                esewaConfig.getMerchantCode(), esewaConfig.getSecretKey()
        );

        List<Payment> stalePending = paymentRepository
                .findAllByBookingIdAndStatus(bookingId, "PENDING");

        for (Payment stale : stalePending) {
            if (stale.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(10))) {
                stale.setStatus("CANCELLED");
                paymentRepository.save(stale);
            } else {
                throw new RuntimeException("A payment is already in progress for this booking.");
            }
        }

        Payment payment = new Payment();
        payment.setTransactionUuid(transactionUuid);
        payment.setProductCode(esewaConfig.getMerchantCode());
        payment.setAmount(booking.getPrice().doubleValue());
        payment.setStatus("PENDING");
        payment.setUser(currentUser.getUser());
        payment.setBookingId(bookingId);
        payment.setCreatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        String dynamicFailureUrl = esewaConfig.getFailureUrl() + "?bookingId=" + bookingId;

        return new EsewaFormData(
                totalAmount, "0", totalAmount,
                transactionUuid,
                esewaConfig.getMerchantCode(),
                "0", "0",
                esewaConfig.getSuccessUrl(),
                dynamicFailureUrl,
                "total_amount,transaction_uuid,product_code",
                signature,
                esewaConfig.getPaymentUrl()
        );
    }

    public EsewaFormData initiateWorkshopPayment(Long workshopId, CustomUserDetails currentUser) throws Exception {
        WorkshopParticipant wp = participantRepository
                .findByWorkshopIdAndParticipantId(workshopId, currentUser.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Please complete registration first"));

        if (wp.getStatus() != WorkshopParticipantStatus.PENDING_PAYMENT)
            throw new RuntimeException("This registration is not awaiting payment");

        Workshop w = wp.getWorkshop();
        String transactionUuid = UUID.randomUUID().toString();
        String totalAmount = w.getPrice().setScale(2, RoundingMode.HALF_UP).toPlainString();

        String signature = signatureUtil.generateSignature(
                totalAmount, transactionUuid,
                esewaConfig.getMerchantCode(), esewaConfig.getSecretKey()
        );

        wp.setTransactionUuid(transactionUuid);
        participantRepository.save(wp);

        String dynamicFailureUrl = esewaConfig.getFailureUrl() + "?workshopId=" + workshopId;

        return new EsewaFormData(
                totalAmount, "0", totalAmount,
                transactionUuid,
                esewaConfig.getMerchantCode(),
                "0", "0",
                esewaConfig.getSuccessUrl(),
                dynamicFailureUrl,
                "total_amount,transaction_uuid,product_code",
                signature,
                esewaConfig.getPaymentUrl()
        );
    }

    public EsewaFormData initiateBannerPayment(Long bannerId, CustomUserDetails currentUser) throws Exception {
        BannerAd banner = bannerAdRepository.findById(bannerId)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        if (!banner.getAdvertiser().getId().equals(currentUser.getUser().getId()))
            throw new RuntimeException("Not authorized for this banner");

        if (banner.getStatus() != BannerStatus.PENDING_PAYMENT)
            throw new RuntimeException("This banner is not awaiting payment");

        String transactionUuid = UUID.randomUUID().toString();
        String totalAmount = banner.getAmountPaid().setScale(2, RoundingMode.HALF_UP).toPlainString();

        String signature = signatureUtil.generateSignature(
                totalAmount, transactionUuid, esewaConfig.getMerchantCode(), esewaConfig.getSecretKey());

        banner.setTransactionUuid(transactionUuid);
        bannerAdRepository.save(banner);

        String dynamicFailureUrl = esewaConfig.getFailureUrl() + "?bannerId=" + bannerId;

        return new EsewaFormData(
                totalAmount, "0", totalAmount, transactionUuid, esewaConfig.getMerchantCode(),
                "0", "0", esewaConfig.getSuccessUrl(), dynamicFailureUrl,
                "total_amount,transaction_uuid,product_code", signature, esewaConfig.getPaymentUrl()
        );
    }

    public EsewaFormData initiateBoostPayment(Long boostId, CustomUserDetails currentUser) throws Exception {
        PhotographerBoost boost = boostRepository.findById(boostId)
                .orElseThrow(() -> new RuntimeException("Boost not found"));

        if (!boost.getPhotographer().getId().equals(currentUser.getUser().getId()))
            throw new RuntimeException("Not authorized for this boost");

        if (boost.getStatus() != BannerStatus.PENDING_PAYMENT)
            throw new RuntimeException("This boost is not awaiting payment");

        String transactionUuid = UUID.randomUUID().toString();
        String totalAmount = boost.getAmountPaid().setScale(2, RoundingMode.HALF_UP).toPlainString();

        String signature = signatureUtil.generateSignature(
                totalAmount, transactionUuid, esewaConfig.getMerchantCode(), esewaConfig.getSecretKey());

        boost.setTransactionUuid(transactionUuid);
        boostRepository.save(boost);

        String dynamicFailureUrl = esewaConfig.getFailureUrl() + "?boostId=" + boostId;

        return new EsewaFormData(
                totalAmount, "0", totalAmount, transactionUuid, esewaConfig.getMerchantCode(),
                "0", "0", esewaConfig.getSuccessUrl(), dynamicFailureUrl,
                "total_amount,transaction_uuid,product_code", signature, esewaConfig.getPaymentUrl()
        );
    }

    public void verifyPayment(String encodedData) throws Exception {
        String decoded = new String(Base64.getDecoder().decode(encodedData));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseData = mapper.readValue(decoded, Map.class);

        String signedFieldNames = responseData.get("signed_field_names");
        String receivedSig      = responseData.get("signature");

        // Build & verify signature
        String[] fields = signedFieldNames.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            sb.append(field).append("=").append(responseData.get(field));
            if (i < fields.length - 1) sb.append(",");
        }

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                esewaConfig.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String expectedSig = Base64.getEncoder().encodeToString(
                mac.doFinal(sb.toString().getBytes(StandardCharsets.UTF_8)));

        if (!expectedSig.equals(receivedSig))
            throw new RuntimeException("Invalid eSewa signature");

        String transactionUuid = responseData.get("transaction_uuid");
        String totalAmount     = responseData.get("total_amount");
        String productCode     = responseData.get("product_code");

        // Cross-check with eSewa status API
        String verifyUrl = esewaConfig.getVerifyUrl()
                + "?product_code=" + productCode
                + "&transaction_uuid=" + transactionUuid
                + "&total_amount=" + totalAmount;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> body = restTemplate.getForEntity(verifyUrl, Map.class).getBody();

        if (!"COMPLETE".equals(body.get("status")))
            throw new RuntimeException("Payment not completed on eSewa side");

        // ── Route by transaction type ─────────────────────────────────────────
        Optional<Payment> paymentOpt = paymentRepository.findByTransactionUuid(transactionUuid);
        Optional<WorkshopParticipant> wpOpt = participantRepository.findByTransactionUuid(transactionUuid);
        Optional<BannerAd> bannerOpt = bannerAdRepository.findByTransactionUuid(transactionUuid);
        Optional<PhotographerBoost> boostOpt = boostRepository.findByTransactionUuid(transactionUuid);

        if (paymentOpt.isPresent()) {
            completeBookingPayment(paymentOpt.get());
        } else if (wpOpt.isPresent()) {
            completeWorkshopPayment(wpOpt.get());
        } else if (bannerOpt.isPresent()) {
            completeBannerPayment(bannerOpt.get());
        } else if (boostOpt.isPresent()) {
            completeBoostPayment(boostOpt.get());
        } else {
            throw new RuntimeException("No record found for transaction: " + transactionUuid);
        }
    }

    private void completeBannerPayment(BannerAd banner) {
        if (banner.getStatus() == BannerStatus.PENDING_REVIEW || banner.getStatus() == BannerStatus.ACTIVE) return; // idempotent

        banner.setStatus(BannerStatus.PENDING_REVIEW);
        bannerAdRepository.save(banner);

        // ✅ declare full amount as platform revenue — no photographer split for ads
        BigDecimal total = banner.getAmountPaid().setScale(2, RoundingMode.HALF_UP);

        Payout payout = new Payout();
        payout.setSourceType("BANNER");
        payout.setTotalAmount(total);
        payout.setCommissionAmount(total);           // 100% platform revenue
        payout.setPhotographerAmount(BigDecimal.ZERO);
        payout.setStatus("PENDING");
        payoutRepository.save(payout);

        notificationService.create(
                banner.getAdvertiser(),
                banner.getAdvertiser(),
                "BANNER_PENDING_REVIEW",
                "Payment received for \"" + banner.getTitle() + "\" — your banner is awaiting admin review",
                "/dashboard/ads/" + banner.getId()
        );
    }

    private void completeBoostPayment(PhotographerBoost boost) {
        if (boost.getStatus() == BannerStatus.ACTIVE || boost.getStatus() == BannerStatus.EXPIRED) return; // idempotent

        LocalDateTime now = LocalDateTime.now();

        Optional<PhotographerBoost> currentActive = boostRepository
                .findFirstByPhotographerIdAndStatusAndEndAtAfterOrderByEndAtDesc(
                        boost.getPhotographer().getId(), BannerStatus.ACTIVE, now);

        LocalDateTime newEndAt;

        if (currentActive.isPresent() && !currentActive.get().getId().equals(boost.getId())) {
            // ✅ already has a running boost — extend it instead of creating a second ACTIVE row
            PhotographerBoost active = currentActive.get();
            newEndAt = active.getEndAt().plusDays(boost.getDaysPurchased());
            active.setEndAt(newEndAt);
            boostRepository.save(active);

            // this purchase joins boost history with the campaign's new end date,
            // but the row above stays the one true ACTIVE boost
            boost.setStartAt(now);
            boost.setEndAt(newEndAt);
            boost.setStatus(BannerStatus.EXPIRED);
            boostRepository.save(boost);

            notificationService.create(
                    boost.getPhotographer(),
                    boost.getPhotographer(),
                    "BOOST_EXTENDED",
                    "Your featured placement was extended by " + boost.getDaysPurchased()
                            + " day(s) — now featured until " + newEndAt.toLocalDate(),
                    "/dashboard/boost"
            );
        } else {
            // no existing active boost — activate normally
            newEndAt = now.plusDays(boost.getDaysPurchased());
            boost.setStatus(BannerStatus.ACTIVE);
            boost.setStartAt(now);
            boost.setEndAt(newEndAt);
            boostRepository.save(boost);

            notificationService.create(
                    boost.getPhotographer(),
                    boost.getPhotographer(),
                    "BOOST_ACTIVE",
                    "You're now featured at the top of the photographer list for " + boost.getDaysPurchased() + " day(s)",
                    "/dashboard/boost"
            );
        }

        // ✅ declare full amount as platform revenue — no photographer split for boosts either
        BigDecimal total = boost.getAmountPaid().setScale(2, RoundingMode.HALF_UP);

        Payout payout = new Payout();
        payout.setPhotographer(boost.getPhotographer());   // ✅ fixes "photographer_id cannot be null"
        payout.setSourceType("BOOST");
        payout.setTotalAmount(total);
        payout.setCommissionAmount(total);
        payout.setPhotographerAmount(BigDecimal.ZERO);
        payout.setStatus("PENDING");
        payoutRepository.save(payout);
    }

    private void completeBookingPayment(Payment payment) {
        if ("COMPLETED".equals(payment.getStatus())) return; // idempotent

        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        if (payment.getBookingId() != null) {
            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setPayment(payment);
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setEscrowStatus(EscrowStatus.HELD);
            bookingRepository.save(booking);

            notificationService.create(
                    booking.getPhotographer(),
                    booking.getClient(),
                    "PAYMENT_RECEIVED",
                    "Payment received for \"" + booking.getEventTitle()
                            + "\" — funds held in escrow",
                    "/dashboard/bookings/" + booking.getId()
            );
        }
    }

    private void completeWorkshopPayment(WorkshopParticipant wp) {
        if (wp.getStatus() == WorkshopParticipantStatus.CONFIRMED) return; // idempotent

        wp.setStatus(WorkshopParticipantStatus.CONFIRMED);
        wp.setPaidAt(LocalDateTime.now());
        participantRepository.save(wp);

        Workshop w = wp.getWorkshop();
//        workshopRepository.confirmSeat(w.getId());

        // ✅ create payout record with 12% commission held for the photographer
        BigDecimal total = w.getPrice().setScale(2, RoundingMode.HALF_UP);
        BigDecimal commission = total.multiply(WORKSHOP_COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal photographerAmount = total.subtract(commission);

        Payout payout = new Payout();
        payout.setWorkshop(w);
        payout.setSourceType("WORKSHOP");
        payout.setPhotographer(w.getPhotographer());
        payout.setTotalAmount(total);
        payout.setCommissionAmount(commission);
        payout.setPhotographerAmount(photographerAmount);
        payout.setStatus("PENDING");
        payoutRepository.save(payout);

        notificationService.create(
                w.getPhotographer(),
                wp.getParticipant(),
                "WORKSHOP_JOINED",
                wp.getParticipant().getFullName() + " joined your workshop \""
                        + w.getTitle() + "\" — "
                        + w.getSeatsAvailable() + " seat(s) remaining",
                "/dashboard/workshops/" + w.getId()
        );
    }

    public void cancelPendingPayment(Long bookingId) {
        paymentRepository.findAllByBookingIdAndStatus(bookingId, "PENDING")
                .forEach(p -> {
                    p.setStatus("CANCELLED");
                    paymentRepository.save(p);
                });
    }

    @Transactional
    public void cancelPendingWorkshopPayment(Long workshopId) {
        participantRepository
                .findByWorkshopIdAndStatus(workshopId, WorkshopParticipantStatus.PENDING_PAYMENT)
                .forEach(wp -> {
                    wp.setStatus(WorkshopParticipantStatus.CANCELLED);
                    participantRepository.save(wp);
                    workshopRepository.releaseSeat(workshopId);
                });
    }

    public void cancelPendingBannerPayment(Long bannerId) {
        bannerAdRepository.findById(bannerId).ifPresent(banner -> {
            if (banner.getStatus() == BannerStatus.PENDING_PAYMENT
                    && banner.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(PAYMENT_GRACE_PERIOD_MINUTES))) {
                banner.setStatus(BannerStatus.CANCELLED);
                bannerAdRepository.save(banner);
            }
            // else: still within the grace window — leave it PENDING_PAYMENT so the user can retry
        });
    }

    public void cancelPendingBoostPayment(Long boostId) {
        boostRepository.findById(boostId).ifPresent(boost -> {
            if (boost.getStatus() == BannerStatus.PENDING_PAYMENT
                    && boost.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(PAYMENT_GRACE_PERIOD_MINUTES))) {
                boost.setStatus(BannerStatus.CANCELLED);
                boostRepository.save(boost);
            }
            // else: still within the grace window — leave it PENDING_PAYMENT so the user can retry
        });
    }
}