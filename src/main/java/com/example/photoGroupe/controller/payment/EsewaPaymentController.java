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

    // eSewa redirects here on success (with ?data=<base64>)
    @GetMapping("/success")
    public void success(@RequestParam String data, HttpServletResponse response) throws Exception {
        System.out.println("=== eSewa success callback received ===");
        System.out.println("data param: " + data);
        try {
            esewaPaymentService.verifyAndCompletePayment(data);
            System.out.println("=== Payment verified and completed ===");
            response.sendRedirect("http://localhost:5173/payment/esewa/success");
        } catch (Exception e) {
            System.out.println("=== Payment verification FAILED: " + e.getMessage() + " ===");
            e.printStackTrace();
            response.sendRedirect("http://localhost:5173/payment/esewa/failure?reason="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @GetMapping("/failure")
    public void failure(HttpServletResponse response) throws IOException {
        response.sendRedirect("http://localhost:5173/payment/esewa/failure");
    }
}