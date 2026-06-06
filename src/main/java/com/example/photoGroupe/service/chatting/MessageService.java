package com.example.photoGroupe.service.chatting;

import com.example.photoGroupe.dto.chatiing.ConversationResponse;
import com.example.photoGroupe.dto.chatiing.MessageResponse;
import com.example.photoGroupe.dto.chatiing.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {

    MessageResponse sendMessage(SendMessageRequest request, Long senderId);

    Page<MessageResponse> getMessages(Long conversationId, int page, int size, Long currentUserId);

    List<MessageResponse> pollMessages(Long conversationId, LocalDateTime since, Long currentUserId);

    List<ConversationResponse> getConversations(Long currentUserId);

    void markAsRead(Long conversationId, Long currentUserId);

    long getTotalUnread(Long currentUserId);

    void deleteMessage(Long messageId, Long currentUserId);

    MessageResponse sendImageMessage(Long receiverId, MultipartFile image, Long senderId) throws IOException;
}