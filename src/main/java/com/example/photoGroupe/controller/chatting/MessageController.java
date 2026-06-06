package com.example.photoGroupe.controller.chatting;

import com.example.photoGroupe.dto.chatiing.ConversationResponse;
import com.example.photoGroupe.dto.chatiing.MessageResponse;
import com.example.photoGroupe.dto.chatiing.SendMessageRequest;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.chatting.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ── REST: Send message ────────────────────────────────────────────────
    @PostMapping("/send")
    public ResponseEntity<MessageResponse> send(
            @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(messageService.sendMessage(request, currentUser.getId()));
    }

    // ── REST: Get paginated messages ──────────────────────────────────────
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                messageService.getMessages(conversationId, page, size, currentUser.getId()));
    }

    // ── REST: Polling fallback ────────────────────────────────────────────
    // GET /api/messages/poll/{conversationId}?since=2025-01-01T00:00:00
    @GetMapping("/poll/{conversationId}")
    public ResponseEntity<List<MessageResponse>> poll(
            @PathVariable Long conversationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                messageService.pollMessages(conversationId, since, currentUser.getId()));
    }

    // ── REST: Get all conversations ───────────────────────────────────────
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(messageService.getConversations(currentUser.getId()));
    }

    // ── REST: Mark conversation as read ──────────────────────────────────
    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long conversationId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        messageService.markAsRead(conversationId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    // ── REST: Total unread count ──────────────────────────────────────────
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> totalUnread(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                Map.of("unread", messageService.getTotalUnread(currentUser.getId())));
    }

    // ── REST: Delete message ──────────────────────────────────────────────
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        messageService.deleteMessage(messageId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // POST /api/users/messages/send/image
    @PostMapping(value = "/send/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> sendImage(
            @RequestParam("receiverId") Long receiverId,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal CustomUserDetails currentUser) throws IOException {

        return ResponseEntity.ok(
                messageService.sendImageMessage(receiverId, image, currentUser.getId()));
    }


    @MessageMapping("/chat.send")
    public void sendViaWebSocket(SendMessageRequest request,
                                 java.security.Principal principal) {
        String email = principal.getName();
        // Look up user id from email — handled inside service
        // This just re-routes to the same service method
        messageService.sendMessage(request,
                extractUserIdFromPrincipal(principal));
    }

    // ── WebSocket: Typing indicator ───────────────────────────────────────
    // Client sends to: /app/chat.typing
    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload,
                       java.security.Principal principal) {
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        // Push typing event to receiver
        // (inject SimpMessagingTemplate here if needed)
    }

    private Long extractUserIdFromPrincipal(java.security.Principal principal) {
        if (principal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth) {
            if (auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getId();
            }
        }
        throw new RuntimeException("Could not extract user from WebSocket principal");
    }
}
