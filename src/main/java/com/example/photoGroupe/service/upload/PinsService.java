package com.example.photoGroupe.service.upload;

import com.example.photoGroupe.dto.pins.*;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface PinsService {

    PinResponse createPin(PinRequest request, Long currentUserId) throws IOException;

    PinResponse getPin(Long pinId, Long currentUserId);

    PinResponse updatePin(Long pinId, PinRequest request, Long currentUserId) throws IOException;

    Page<PinResponse> getFeed(int page, int size, Long currentUserId);

    Page<PinResponse> getUserPins(Long userId, int page, int size, Long currentUserId);

    Page<PinResponse> searchByTag(String tag, int page, int size, Long currentUserId);

    PinResponse toggleLike(Long pinId, Long currentUserId);

    CommentResponse addComment(Long pinId, CommentRequest request, Long currentUserId);

    Page<CommentResponse> getComments(Long pinId, int page, int size, Long currentUserId);

    void deleteComment(Long commentId, Long currentUserId, Role currentUserRole);

    void deletePin(Long pinId, Long currentUserId, Role currentUserRole) throws IOException;

    Page<PinResponse> getPinsByCategoryName(String name, int page, int size, Long currentUserId);

    Page<PinResponse> getPinsByCategorySlug(String slug, int page, int size, Long currentUserId);

    CommentResponse updateComment(Long commentId, CommentRequest request, Long currentUserId);

    CommentResponse toggleCommentLike(Long commentId, Long currentUserId);

    CommentResponse replyToComment(Long pinId, Long parentCommentId, CommentRequest request, Long currentUserId);

    PinResponse suspendPin(Long pinId, String reason, User admin);

    PinResponse unsuspendPin(Long pinId, User admin);

    SharePinResponse sharePin(Long pinId, Long currentUserId);

    SavePinResponse toggleSavePin(Long pinId, Long currentUserId);

    Page<PinResponse> getSavedPins(int page, int size, Long currentUserId);

    long getShareCount(Long pinId);
}
