package com.example.photoGroupe.service.chatting;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.photoGroupe.dto.chatiing.ConversationResponse;
import com.example.photoGroupe.dto.chatiing.MessageResponse;
import com.example.photoGroupe.dto.chatiing.SendMessageRequest;
import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.chatting.Conversation;
import com.example.photoGroupe.model.chatting.Message;
import com.example.photoGroupe.model.chatting.MessageType;
import com.example.photoGroupe.repo.PinRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.chatting.ConversationRepository;
import com.example.photoGroupe.repo.chatting.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements  MessageService{

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final SimpMessagingTemplate messagingTemplate;  // WebSocket pusher
    private final Cloudinary  cloudinary;

    @Override
    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request, Long senderId) {

        User sender   = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Get or create conversation
        Conversation conversation = conversationRepository
                .findBetweenUsers(senderId, request.getReceiverId())
                .orElseGet(() -> conversationRepository.save(
                        new Conversation(sender, receiver)));

        Message message = new Message(conversation, sender,
                request.getText(), request.getType());

        // Attach shared pin if present
        if (request.getSharedPinId() != null) {
            Pin pin = pinRepository.findById(request.getSharedPinId())
                    .orElseThrow(() -> new RuntimeException("Pin not found"));
            message.setSharedPin(pin);
            message.setType(MessageType.PIN_SHARE);
        }

        messageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(message);

        // ── Push via WebSocket to receiver ────────────────────────────────
        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),            // Spring uses email (getUsername()) as principal
                "/queue/messages",
                response
        );

        return response;
    }

    // ─── Get Messages (paginated) ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long conversationId, int page,
                                             int size, Long currentUserId) {
        assertConversationMember(conversationId, currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        return messageRepository
                .findByConversationIdAndDeletedFalseOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::toMessageResponse);
    }

    // ─── Poll Messages (since timestamp — polling fallback) ───────────────

    @Override
    @Transactional
    public List<MessageResponse> pollMessages(Long conversationId,
                                              LocalDateTime since, Long currentUserId) {
        assertConversationMember(conversationId, currentUserId);
        markAsRead(conversationId, currentUserId);   // auto-mark read on poll
        return messageRepository
                .findByConversationIdAndDeletedFalseAndCreatedAtAfterOrderByCreatedAtAsc(
                        conversationId, since)
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    // ─── Get Conversations ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getConversations(Long currentUserId) {
        return conversationRepository.findAllByUserId(currentUserId)
                .stream()
                .map(c -> toConversationResponse(c, currentUserId))
                .toList();
    }

    // ─── Mark as Read ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void markAsRead(Long conversationId, Long currentUserId) {
        assertConversationMember(conversationId, currentUserId);
        messageRepository.markAllAsRead(conversationId, currentUserId);
    }

    // ─── Total Unread ─────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnread(Long currentUserId) {
        return messageRepository.countTotalUnread(currentUserId);
    }

    // ─── Delete Message ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getSender().getId().equals(currentUserId))
            throw new RuntimeException("Not authorized to delete this message");

        message.setDeleted(true);
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public MessageResponse sendImageMessage(Long receiverId, MultipartFile image,
                                            Long senderId) throws IOException {
        User sender   = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Conversation conversation = conversationRepository
                .findBetweenUsers(senderId, receiverId)
                .orElseGet(() -> conversationRepository.save(
                        new Conversation(sender, receiver)));

        // ── Upload to Cloudinary ──────────────────────────────────────────
        String publicId = "photogroupe/messages/user_" + senderId + "_"
                + System.currentTimeMillis();

        Map<?, ?> result = cloudinary.uploader().upload(
                image.getBytes(),
                ObjectUtils.asMap(
                        "public_id",     publicId,
                        "resource_type", "image",
                        "quality",       "auto",
                        "fetch_format",  "auto"
                )
        );

        String imageUrl = (String) result.get("secure_url");  // ← Cloudinary URL

        // ── Save message ──────────────────────────────────────────────────
        Message message = new Message(conversation, sender, null, MessageType.IMAGE);
        message.setImageUrl(imageUrl);
        messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(message);

        messagingTemplate.convertAndSendToUser(
                receiver.getEmail(),
                "/queue/messages",
                response
        );

        return response;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private void assertConversationMember(Long conversationId, Long userId) {
        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        boolean isMember = c.getUserOne().getId().equals(userId)
                || c.getUserTwo().getId().equals(userId);
        if (!isMember) throw new RuntimeException("Not a member of this conversation");
    }

    private MessageResponse toMessageResponse(Message message) {
        MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getActualUsername())
                .senderProfilePicture(message.getSender().getProfilePicture())
                .text(message.isDeleted() ? "[deleted]" : message.getText())
                .type(message.getType())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .imageUrl(message.getImageUrl());

        if (message.getSharedPin() != null) {
            builder.sharedPinId(message.getSharedPin().getId())
                    .sharedPinImageUrl(message.getSharedPin().getImageUrl())
                    .sharedPinTitle(message.getSharedPin().getTitle());
        }

        return builder.build();
    }

    private ConversationResponse toConversationResponse(Conversation c, Long currentUserId) {
        User other = c.getOtherUser(currentUserId);
        long unread = messageRepository
                .countByConversationIdAndReadFalseAndSenderIdNot(c.getId(), currentUserId);

        // Last message text
        String lastMessage = c.getMessages().isEmpty() ? null :
                c.getMessages().get(c.getMessages().size() - 1).getText();

        return ConversationResponse.builder()
                .id(c.getId())
                .otherUserId(other.getId())
                .otherUsername(other.getActualUsername())
                .otherProfilePicture(other.getProfilePicture())
                .lastMessage(lastMessage)
                .lastMessageAt(c.getUpdatedAt())
                .unreadCount(unread)
                .build();
    }
}
