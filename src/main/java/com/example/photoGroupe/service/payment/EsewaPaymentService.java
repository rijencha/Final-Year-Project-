package com.example.photoGroupe.service.payment;

import com.example.photoGroupe.config.EsewaConfig;
import com.example.photoGroupe.dto.payment.EsewaFormData;
import com.example.photoGroupe.dto.payment.EsewaPaymentRequest;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.booking.*;
import com.example.photoGroupe.repo.BookingRepository;
import com.example.photoGroupe.repo.payment.BookingPackageRepository;
import com.example.photoGroupe.repo.payment.PaymentRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.util.EsewaSignatureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EsewaPaymentService {

    private final EsewaConfig esewaConfig;
    private final EsewaSignatureUtil signatureUtil;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;
    private final BookingPackageRepository packageRepository;

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

        return new EsewaFormData(
                totalAmount, "0", totalAmount,
                transactionUuid,
                esewaConfig.getMerchantCode(),
                "0", "0",
                esewaConfig.getSuccessUrl(),
                esewaConfig.getFailureUrl(),
                "total_amount,transaction_uuid,product_code",
                signature,
                esewaConfig.getPaymentUrl()
        );
    }

    public void verifyAndCompletePayment(String encodedData) throws Exception {
        String decoded = new String(Base64.getDecoder().decode(encodedData));
//        System.out.println("=== eSewa Decoded Response ===");
//        System.out.println(decoded);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseData = mapper.readValue(decoded, Map.class);

        String signedFieldNames = responseData.get("signed_field_names");
        String receivedSig      = responseData.get("signature");

        // ✅ Build message dynamically from signed_field_names
        String[] fields = signedFieldNames.split(",");
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            messageBuilder.append(field).append("=").append(responseData.get(field));
            if (i < fields.length - 1) messageBuilder.append(",");
        }
        String message = messageBuilder.toString();
        System.out.println("message_used: [" + message + "]");

        // ✅ Verify signature using dynamic message
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(
                esewaConfig.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"
        );
        mac.init(keySpec);
        byte[] hash = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        String expectedSig = Base64.getEncoder().encodeToString(hash);

        System.out.println("expected_sig: " + expectedSig);
        System.out.println("received_sig: " + receivedSig);

        if (!expectedSig.equals(receivedSig)) {
            throw new RuntimeException("Invalid eSewa signature");
        }

        // ✅ Continue with DB update
        String transactionUuid = responseData.get("transaction_uuid");
        String totalAmount     = responseData.get("total_amount");
        String productCode     = responseData.get("product_code");

        // Cross-check with eSewa status API
        String verifyUrl = esewaConfig.getVerifyUrl()
                + "?product_code=" + productCode
                + "&transaction_uuid=" + transactionUuid
                + "&total_amount=" + totalAmount;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> verifyResponse = restTemplate.getForEntity(verifyUrl, Map.class);
        Map<String, Object> body = verifyResponse.getBody();

        if (!"COMPLETE".equals(body.get("status"))) {
            throw new RuntimeException("Payment not completed on eSewa side");
        }

        Payment payment = paymentRepository.findByTransactionUuid(transactionUuid)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));
        if ("COMPLETED".equals(payment.getStatus())) {
            return;
        }
        payment.setStatus("COMPLETED");
        paymentRepository.save(payment);

        // ── NEW: lock funds in escrow on the booking ──
        if (payment.getBookingId() != null) {
            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            booking.setPayment(payment);
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setEscrowStatus(EscrowStatus.HELD);      // 🔒 funds held
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
}