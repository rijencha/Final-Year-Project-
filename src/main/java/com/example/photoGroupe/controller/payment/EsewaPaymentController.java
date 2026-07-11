package com.example.photoGroupe.controller.payment;

import com.example.photoGroupe.dto.payment.EsewaFormData;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.payment.EsewaPaymentService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/users/esewa")
@RequiredArgsConstructor
public class EsewaPaymentController {

    private final EsewaPaymentService esewaPaymentService;

    // Frontend calls this to get signed form data
    @PostMapping("/initiate/{bookingId}")          // ← add path variable
    public ResponseEntity<EsewaFormData> initiate(
            @PathVariable Long bookingId,           // ← bind from path
            @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        return ResponseEntity.ok(
                esewaPaymentService.initiatePayment(null, currentUser, bookingId)
        );
    }

    @PostMapping("/workshop/initiate/{workshopId}")
    public ResponseEntity<EsewaFormData> initiateWorkshopPayment(
            @PathVariable Long workshopId,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        return ResponseEntity.ok(
                esewaPaymentService.initiateWorkshopPayment(workshopId, currentUser)
        );
    }

    @PostMapping("/banner/initiate/{bannerId}")
    public ResponseEntity<EsewaFormData> initiateBannerPayment(
            @PathVariable Long bannerId,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        return ResponseEntity.ok(
                esewaPaymentService.initiateBannerPayment(bannerId, currentUser)
        );
    }

    @PostMapping("/boost/initiate/{boostId}")
    public ResponseEntity<EsewaFormData> initiateBoostPayment(
            @PathVariable Long boostId,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws Exception {
        return ResponseEntity.ok(
                esewaPaymentService.initiateBoostPayment(boostId, currentUser)
        );
    }

    // eSewa redirects here on success (with ?data=<base64>)
    @GetMapping("/success")
    public void success(@RequestParam String data, HttpServletResponse response) throws Exception {
        try {
            esewaPaymentService.verifyPayment(data);   // ← unified method
            response.sendRedirect("http://localhost:5173/payment/esewa/success");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/payment/esewa/failure?reason="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/failure")
    public void failure(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long workshopId,
            @RequestParam(required = false) Long bannerId,
            @RequestParam(required = false) Long boostId,
            HttpServletResponse response) throws IOException {

        try {
            if (bookingId != null)
                esewaPaymentService.cancelPendingPayment(bookingId);

            if (workshopId != null)
                esewaPaymentService.cancelPendingWorkshopPayment(workshopId);

            if (bannerId != null)
                esewaPaymentService.cancelPendingBannerPayment(bannerId);

            if (boostId != null)
                esewaPaymentService.cancelPendingBoostPayment(boostId);
        } catch (Exception e) {
            // Log it so you can still see what went wrong server-side
            e.printStackTrace();
            // still redirect the user, don't leave them on a blank 500 page
        }

        response.sendRedirect("http://localhost:5173/payment/esewa/failure");
    }

}