package com.example.photoGroupe.controller.test;

import com.example.photoGroupe.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestEmailController {
    private final EmailService emailService;

    @GetMapping("/send-test-email")
    public String sendEmail() {

        emailService.sendTestEmail("maharjanr353@gmail.com");

        return "Email sent successfully!";
    }
}
