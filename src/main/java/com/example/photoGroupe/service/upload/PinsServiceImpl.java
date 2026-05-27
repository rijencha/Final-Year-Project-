package com.example.photoGroupe.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.photoGroupe.dto.pins.CommentRequest;
import com.example.photoGroupe.dto.pins.CommentResponse;
import com.example.photoGroupe.dto.pins.PinRequest;
import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.model.*;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PinsServiceImpl implements PinsService{

    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final PinLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationService  notificationService;

    private String[] uploadPinImage(MultipartFile file, Long userId) throws IOException {
        String publicId = "photogroupe/pins/user_" + userId + "_" + System.currentTimeMillis();

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id",     publicId,
                        "overwrite",     false,
                        "resource_type", "image",
                        "quality",       "auto",
                        "fetch_format",  "auto"
                )
        );

        return new String[]{
                (String) result.get("secure_url"),
                (String) result.get("public_id")
        };
    }

    @Override
    @Transactional
    public PinResponse createPin(PinRequest request, Long currentUserId) throws IOException {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String[] upload = uploadPinImage(request.getImage(), currentUserId);

        Pin pin = new Pin(
                upload[0],
                upload[1],
                request.getTitle(),
                request.getDescription(),
                request.getTags(),
                user
        );

        // ── Category (by id, slug, or name — whichever is provided) ──────────
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        } else if (request.getCategorySlug() != null) {
            Category category = categoryRepository.findBySlug(request.getCategorySlug())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        } else if (request.getCategoryName() != null) {
            Category category = categoryRepository.findByName(request.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        }

        pinRepository.save(pin);
        return toResponse(pin, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public PinResponse getPin(Long pinId, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        return toResponse(pin, currentUserId);

    }

    @Override
    @Transactional
    public PinResponse updatePin(Long pinId, PinRequest request, Long currentUserId) throws IOException {
        Pin pin = findActivePin(pinId);
        assertOwner(pin, currentUserId);

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            cloudinary.uploader().destroy(pin.getPublicId(), ObjectUtils.emptyMap());
            String[] upload = uploadPinImage(request.getImage(), currentUserId);
            pin.setImageUrl(upload[0]);
            pin.setPublicId(upload[1]);
        }

        if (request.getTitle()       != null) pin.setTitle(request.getTitle());
        if (request.getDescription() != null) pin.setDescription(request.getDescription());
        if (request.getTags()        != null) pin.setTags(request.getTags());

        // ── Category (by id, slug, or name — whichever is provided) ──────────
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        } else if (request.getCategorySlug() != null) {
            Category category = categoryRepository.findBySlug(request.getCategorySlug())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        } else if (request.getCategoryName() != null) {
            Category category = categoryRepository.findByName(request.getCategoryName())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            pin.setCategory(category);
        }

        pinRepository.save(pin);
        return toResponse(pin, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getFeed(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return pinRepository
                .findByDeletedFalseOrderByCreatedAtDesc(pageable)
                .map(p -> toResponse(p, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getUserPins(Long userId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return pinRepository
                .findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                .map(p -> toResponse(p, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> searchByTag(String tag, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return pinRepository
                .findByTag(tag, pageable)
                .map(p -> toResponse(p, currentUserId));
    }

    @Override
    @Transactional
    public PinResponse toggleLike(Long pinId, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean[] isLiking = {false};

        likeRepository.findByUserIdAndPinId(currentUserId, pinId)
                .ifPresentOrElse(
                        likeRepository::delete,                        // already liked → unlike
                        () -> {
                            likeRepository.save(new PinLike(user, pin)); // not liked → like
                            isLiking[0] = true;                        }
                );
        if (isLiking[0] && !pin.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    pin.getUser(),   // ← pin.getAuthor() if that method exists, else pin.getUser()
                    user,
                    "LIKE",
                    user.getFullName() + " liked your pin \"" + pin.getTitle() + "\"",
                    "/pin/" + pin.getId()
            );
        }

        // Re-fetch to get updated counts
        return toResponse(pinRepository.findById(pinId).orElseThrow(), currentUserId);
    }

    @Override
    @Transactional
    public CommentResponse addComment(Long pinId, CommentRequest request, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment(request.getText(), user, pin);
        commentRepository.save(comment);
        if (!pin.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    pin.getUser(),
                    user,
                    "COMMENT",
                    user.getFullName() + " commented on your pin",
                    "/pin/" + pin.getId()
            );
        }
        return toCommentResponse(comment, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long pinId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return commentRepository
                .findByPinIdAndDeletedFalseOrderByCreatedAtAsc(pinId, pageable)
                .map(c -> toCommentResponse(c, currentUserId));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long currentUserId, Role currentUserRole) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        boolean isOwner  = comment.getUser().getId().equals(currentUserId);
        boolean isPinOwner = comment.getPin().getUser().getId().equals(currentUserId);
        boolean isAdmin  = currentUserRole == Role.ADMIN || currentUserRole == Role.SUPER_ADMIN;

        if (!isOwner && !isPinOwner && !isAdmin)
            throw new AccessDeniedException("Not authorized to delete this comment");

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void deletePin(Long pinId, Long currentUserId, Role currentUserRole) throws IOException {
        Pin pin = findActivePin(pinId);

        // Owner OR admin can delete
        boolean isOwner = pin.getUser().getId().equals(currentUserId);
        boolean isAdmin = currentUserRole == Role.ADMIN || currentUserRole == Role.SUPER_ADMIN;
        if (!isOwner && !isAdmin) throw new AccessDeniedException("Not authorized to delete this pin");

        cloudinary.uploader().destroy(pin.getPublicId(), ObjectUtils.emptyMap()); // remove from Cloudinary
        pin.setDeleted(true);
        pinRepository.save(pin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getPinsByCategoryName(String name, int page, int size, Long currentUserId) {
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Category not found with name: " + name));
        Pageable pageable = PageRequest.of(page, size);
        return pinRepository
                .findByCategoryIdAndDeletedFalse(category.getId(), pageable)
                .map(p -> toResponse(p, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getPinsByCategorySlug(String slug, int page, int size, Long currentUserId) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Pageable pageable = PageRequest.of(page, size);
        return pinRepository
                .findByCategoryIdAndDeletedFalse(category.getId(), pageable)
                .map(p -> toResponse(p, currentUserId));
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(currentUserId))
            throw new RuntimeException("Not authorized to edit this comment");

        if (comment.isDeleted())
            throw new RuntimeException("Cannot edit a deleted comment");

        comment.setText(request.getText());
        Comment saved = commentRepository.save(comment);
        return toCommentResponse(saved, currentUserId);
    }

// ─── Toggle Comment Like ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse toggleCommentLike(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isLiking;
        if (comment.getLikedBy().contains(user)) {
            comment.getLikedBy().remove(user);
            isLiking = false; // unliking
        } else {
            comment.getLikedBy().add(user);
            isLiking = true;  // liking
        }

        Comment saved = commentRepository.save(comment);

        // Only notify on like, not unlike, and not self-like
        if (isLiking && !comment.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    comment.getUser(),
                    user,
                    "COMMENT_LIKE",
                    user.getFullName() + " liked your comment",
                    "/pin/" + comment.getPin().getId()
            );
        }

        return toCommentResponse(saved, currentUserId);
    }

// ─── Reply to Comment ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CommentResponse replyToComment(Long pinId, Long parentCommentId, CommentRequest request, Long currentUserId) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new RuntimeException("Pin not found"));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment parent = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment reply = new Comment(request.getText(), user, pin, parent);
        Comment saved = commentRepository.save(reply);
        if (!parent.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    parent.getUser(),
                    user,
                    "REPLY",
                    user.getFullName() + " replied to your comment",
                    "/pin/" + pin.getId()
            );
        }
        return toCommentResponse(saved, currentUserId);
    }


    // ─── Private helpers ──────────────────────────────────────────────────

    private Pin findActivePin(Long pinId) {
        return pinRepository.findById(pinId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Pin not found"));
    }

    private void assertOwner(Pin pin, Long userId) {
        if (!pin.getUser().getId().equals(userId))
            throw new AccessDeniedException("Not authorized");
    }

    // ─── Mapper ───────────────────────────────────────────────────────────

    private PinResponse toResponse(Pin pin, Long currentUserId) {
        PinResponse r = new PinResponse();
        r.setId(pin.getId());
        r.setImageUrl(pin.getImageUrl());
        r.setTitle(pin.getTitle());
        r.setDescription(pin.getDescription());
        r.setTags(pin.getTags());
        r.setCreatedAt(pin.getCreatedAt());

        if (pin.getCategory() != null) {
            r.setCategoryId(pin.getCategory().getId());
            r.setCategoryName(pin.getCategory().getName());
            r.setCategorySlug(pin.getCategory().getSlug());
        }

        r.setAuthorId(pin.getUser().getId());
        r.setAuthorUsername(pin.getUser().getActualUsername());
        r.setAuthorProfilePicture(pin.getUser().getProfilePicture());
        r.setAuthorRole(pin.getUser().getRole().name());
        r.setAuthorName(pin.getUser().getFullName()); // or getFirstName() + " " + getLastName()

        // null until likes/comments are implemented
        r.setLikeCount((int) likeRepository.countByPinId(pin.getId()));
        r.setCommentCount((int) commentRepository.countByPinIdAndDeletedFalse(pin.getId()));
        // Did the current user like this?
        r.setLikedByCurrentUser(
                currentUserId != null &&
                        likeRepository.existsByUserIdAndPinId(currentUserId, pin.getId())
        );

        return r;
    }

    private CommentResponse toCommentResponse(Comment comment, Long currentUserId) {
        CommentResponse res = new CommentResponse();
        res.setId(comment.getId());
        res.setText(comment.isDeleted() ? "[deleted]" : comment.getText());
        res.setDeleted(comment.isDeleted());
        res.setEdited(comment.getUpdatedAt() != null);
        res.setAuthorId(comment.getUser().getId());
        res.setAuthorUsername(comment.getUser().getUsername());
        res.setAuthorProfilePicture(comment.getUser().getProfilePicture());
        res.setCreatedAt(comment.getCreatedAt());
        res.setUpdatedAt(comment.getUpdatedAt());
        res.setLikeCount(comment.getLikedBy().size());
        res.setLikedByCurrentUser(
                comment.getLikedBy().stream().anyMatch(u -> u.getId().equals(currentUserId))
        );
        res.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);

        // Map replies recursively
        List<CommentResponse> replies = comment.getReplies().stream()
                .filter(r -> !r.isDeleted())
                .map(r -> toCommentResponse(r, currentUserId))
                .toList();
        res.setReplies(replies);

        return res;
    }
}
