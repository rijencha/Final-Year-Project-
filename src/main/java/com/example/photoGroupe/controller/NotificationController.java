package com.example.photoGroupe.controller;

import com.example.photoGroupe.dto.notification.NotificationResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// controller/NotificationController.java
@RestController
@RequestMapping("/api/users/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

//    @GetMapping
//    public ResponseEntity<List<NotificationResponse>> getAll(
//            @AuthenticationPrincipal CustomUserDetails currentUser
//    ) {
//        return ResponseEntity.ok(notificationService.getForUser(currentUser.getId()));
//    }
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        System.out.println("USER ID: " + currentUser.getId()); // ← add this
        List<NotificationResponse> list = notificationService.getForUser(currentUser.getId());
        System.out.println("LIST SIZE: " + list.size()); // ← add this
        return ResponseEntity.ok(list);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        notificationService.markAllRead(currentUser.getId());
        return ResponseEntity.ok().build();
    }
}
