package com.example.photoGroupe.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.photoGroupe.dto.album.DownloadResponse;
import com.example.photoGroupe.dto.pins.*;
import com.example.photoGroupe.model.*;
import com.example.photoGroupe.model.pins.PinDownload;
import com.example.photoGroupe.model.pins.PinShare;
import com.example.photoGroupe.model.pins.PinView;
import com.example.photoGroupe.model.pins.SavedPin;
import com.example.photoGroupe.model.restrict.RestrictionType;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.pins.PinDownloadRepository;
import com.example.photoGroupe.repo.pins.PinViewRepository;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.restrict.FeedExclusionService;
import com.example.photoGroupe.service.restrict.UserRestrictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final SavedPinRepository savedPinRepository;
    private final PinShareRepository pinShareRepository;
    private final PinDownloadRepository pinDownloadRepository;
    private final PinViewRepository pinViewRepository;
    private final FeedExclusionService feedExclusionService;
    private final UserRestrictionService restrictionService;

    @Value("${app.base-url}")          // e.g. https://yourapp.com  (set in application.properties)
    private String baseUrl;

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
    public PinResponse createPin(PinRequest request, Long currentUserId, boolean albumOnly) throws IOException {
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
        pin.setAlbumOnly(albumOnly);   // ← new line

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
    @Transactional
    public PinResponse getPin(Long pinId, Long currentUserId) {
        Pin pin = findActivePin(pinId);

        if (currentUserId != null && !currentUserId.equals(pin.getUser().getId())) {
            User viewer = userRepository.findById(currentUserId).orElse(null);
            if (viewer != null) {
                Optional<PinView> existingView =
                        pinViewRepository.findByPinIdAndUserId(pinId, currentUserId);

                boolean shouldCount = existingView
                        .map(pv -> pv.getLastViewedAt()
                                .isBefore(LocalDateTime.now().minusHours(24)))
                        .orElse(true); // no record → first view → count it

                if (shouldCount) {
                    pinRepository.incrementViewCount(pinId);
                    pin.setViewCount(pin.getViewCount() + 1);

                    if (existingView.isPresent()) {
                        existingView.get().setLastViewedAt(LocalDateTime.now());
                        pinViewRepository.save(existingView.get());
                    } else {
                        pinViewRepository.save(new PinView(pin, viewer));
                    }
                }
            }
        }

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
        User user = userRepository.findById(currentUserId).orElseThrow();
        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        String interestsStr = user.getInterests();

        if (interestsStr == null || interestsStr.isBlank()) {
            List<Pin> filtered = pinRepository
                    .findByDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(
                            PageRequest.of(page, size + exclusions.pinIds().size() + exclusions.userIds().size() + exclusions.categoryIds().size()))
                    .getContent().stream()
                    .filter(p -> !exclusions.excludes(p))
                    .limit(size)
                    .toList();

            return new org.springframework.data.domain.PageImpl<>(
                    filtered.stream().map(p -> toResponse(p, currentUserId)).toList(),
                    PageRequest.of(page, size), filtered.size());
        }

        List<String> interests = List.of(interestsStr.split(","));

        int interestSize = (int) Math.ceil(size * 0.7);  // 14
        int otherSize    = size - interestSize;           // 6

        // Fetch a bit extra to survive de-dup losses
        List<Pin> interestPins = pinRepository.findByInterestsRaw(
                        interests, PageRequest.of(page, interestSize + 5))
                .stream().filter(p -> !exclusions.excludes(p)).toList();

        List<Pin> otherPins = pinRepository.findExcludingInterestsRaw(
                        interests, PageRequest.of(page, otherSize + 5))
                .stream().filter(p -> !exclusions.excludes(p)).toList();

        // De-duplicate by pin id across both lists
        Set<Long> seen = new java.util.LinkedHashSet<>();
        List<Pin> merged = new java.util.ArrayList<>();

        for (Pin p : interestPins) {
            if (seen.add(p.getId())) merged.add(p);
            if (merged.size() == interestSize) break;
        }
        for (Pin p : otherPins) {
            if (seen.add(p.getId())) merged.add(p);
            if (merged.size() == size) break;
        }

        // If interest pins didn't fill quota, backfill with more other pins
        if (merged.size() < size) {
            List<Pin> backfill = pinRepository.findExcludingInterestsRaw(
                            interests, PageRequest.of(page, size))
                    .stream().filter(p -> !exclusions.excludes(p)).toList();
            for (Pin p : backfill) {
                if (seen.add(p.getId())) merged.add(p);
                if (merged.size() == size) break;
            }
        }

        List<PinResponse> content = merged.stream()
                .map(p -> toResponse(p, currentUserId))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(content, pageable, content.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getUserPins(Long userId, int page, int size, Long currentUserId) {
        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        Pageable pageable = PageRequest.of(page, size + exclusions.pinIds().size());

        List<Pin> filtered = pinRepository
                .findByUserIdAndDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(userId, pageable)
                .getContent().stream()
                .filter(p -> !exclusions.pinIds().contains(p.getId()))
                .limit(size)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(p -> toResponse(p, currentUserId)).toList(),
                PageRequest.of(page, size), filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> searchByTag(String tag, int page, int size, Long currentUserId) {
        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        Pageable pageable = PageRequest.of(page, size + exclusions.pinIds().size() + exclusions.userIds().size() + 5);

        List<Pin> filtered = pinRepository.findByTag(tag, pageable).getContent().stream()
                .filter(p -> !exclusions.excludes(p))
                .limit(size)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(p -> toResponse(p, currentUserId)).toList(),
                PageRequest.of(page, size), filtered.size());
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
        restrictionService.assertNotRestricted(pin.getUser().getId(), currentUserId, RestrictionType.COMMENT);

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
                .findByPinIdAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(pinId, pageable)
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

        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        Pageable pageable = PageRequest.of(page, size + exclusions.pinIds().size() + exclusions.userIds().size() + exclusions.categoryIds().size());

        List<Pin> filtered = pinRepository
                .findByCategoryIdAndDeletedFalseAndAlbumOnlyFalse(category.getId(), pageable)
                .getContent().stream()
                .filter(p -> !exclusions.excludes(p))
                .limit(size)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(p -> toResponse(p, currentUserId)).toList(),
                PageRequest.of(page, size), filtered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getPinsByCategorySlug(String slug, int page, int size, Long currentUserId) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        Pageable pageable = PageRequest.of(page, size + exclusions.pinIds().size());

        List<Pin> filtered = pinRepository
                .findByCategoryIdAndDeletedFalseAndAlbumOnlyFalse(category.getId(), pageable)
                .getContent().stream()
                .filter(p -> !exclusions.excludes(p))
                .limit(size)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(p -> toResponse(p, currentUserId)).toList(),
                PageRequest.of(page, size), filtered.size());
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

        restrictionService.assertNotRestricted(pin.getUser().getId(), currentUserId, RestrictionType.COMMENT);

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

    @Override
    @Transactional
    public PinResponse suspendPin(Long pinId, String reason, User admin) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new RuntimeException("Pin not found"));

        if (pin.isSuspended())
            throw new RuntimeException("Pin is already suspended");

        pin.setSuspended(true);
        pin.setSuspensionReason(reason);
        pin.setSuspendedAt(LocalDateTime.now());
        pin.setSuspendedBy(admin);
        pinRepository.save(pin);

        // Notify pin owner
        notificationService.create(
                pin.getUser(),
                admin,
                "PIN_SUSPENDED",
                "Your pin \"" + pin.getTitle() + "\" has been suspended. Reason: " + reason,
                "/pin/" + pin.getId()
        );

        return toResponse(pin, admin.getId());
    }

    @Override
    @Transactional
    public PinResponse unsuspendPin(Long pinId, User admin) {
        Pin pin = pinRepository.findById(pinId)
                .orElseThrow(() -> new RuntimeException("Pin not found"));

        if (!pin.isSuspended())
            throw new RuntimeException("Pin is not suspended");

        pin.setSuspended(false);
        pin.setSuspensionReason(null);
        pin.setSuspendedAt(null);
        pin.setSuspendedBy(null);
        pinRepository.save(pin);

        // Notify pin owner
        notificationService.create(
                pin.getUser(),
                admin,
                "PIN_UNSUSPENDED",
                "Your pin \"" + pin.getTitle() + "\" suspension has been lifted",
                "/pin/" + pin.getId()
        );

        return toResponse(pin, admin.getId());
    }

    // ─── Share Pin ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SharePinResponse sharePin(Long pinId, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String shareLink = baseUrl + "/pin/" + pinId;

        PinShare share = new PinShare(pin, user, shareLink);
        pinShareRepository.save(share);

//        pinShareRepository.findByPinAndUser(pinId, currentUserId)
//                .orElseGet(() -> pinShareRepository.save(new PinShare(pin, user, shareLink)));

        // Notify pin owner (skip self-share)
        if (!pin.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    pin.getUser(),
                    user,
                    "SHARE",
                    user.getFullName() + " shared your pin \"" + pin.getTitle() + "\"",
                    "/pin/" + pinId
            );
        }

        return SharePinResponse.builder()
                .pinId(pinId)
                .shareLink(shareLink)
                .totalShares(pinShareRepository.countDistinctSharersByPinId(pinId))
                .build();
    }

// ─── Save / Unsave Pin (Bookmark toggle) ─────────────────────────────────────

    @Override
    @Transactional
    public SavePinResponse toggleSavePin(Long pinId, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean saved;
        savedPinRepository.findByUserIdAndPinId(currentUserId, pinId)
                .ifPresentOrElse(
                        savedPin -> savedPinRepository.delete(savedPin),   // already saved → unsave
                        () -> savedPinRepository.save(new SavedPin(user, pin)) // not saved → save
                );

        // Re-check state after toggle
        saved = savedPinRepository.existsByUserIdAndPinId(currentUserId, pinId);

        return SavePinResponse.builder()
                .pinId(pinId)
                .saved(saved)
                .totalSaves(savedPinRepository.countByPinId(pinId))
                .build();
    }

// ─── Get Saved Pins ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getSavedPins(int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        return savedPinRepository
                .findByUserIdOrderBySavedAtDesc(currentUserId, pageable)
                .map(savedPin -> toResponse(savedPin.getPin(), currentUserId));
    }

// ─── Get Share Count ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public long getShareCount(Long pinId) {
        findActivePin(pinId);  // validates pin exists and is not deleted/suspended
        return pinShareRepository.countDistinctSharersByPinId(pinId);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<PinResponse> getTopPins(int limit, Long currentUserId) {
//        return pinRepository.findByDeletedFalseAndSuspendedFalseAndAlbumOnlyFalse()
//                .stream()
//                .map(p -> toResponse(p, currentUserId))
//                .sorted(Comparator.comparingInt(
//                        (PinResponse p) -> p.getLikeCount() + p.getCommentCount() + p.getShareCount() + p.getSaveCount()
//                ).reversed())
//                .limit(limit)
//                .toList();
//    }
    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getTopPins(int limit, Long currentUserId) {
        var exclusions = feedExclusionService.getExclusionSet(currentUserId);
        return pinRepository.findByDeletedFalseAndSuspendedFalseAndAlbumOnlyFalse()
                .stream()
                .filter(p -> !exclusions.excludes(p))
                .map(p -> toResponse(p, currentUserId))
                .sorted(Comparator.comparingInt(
                        (PinResponse p) -> p.getLikeCount() + p.getCommentCount() + p.getShareCount() + p.getSaveCount()
                ).reversed())
                .limit(limit)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getTopPinsByUser(Long userId, int limit, Long currentUserId) {
        return pinRepository
                .findByUserIdAndDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(p -> toResponse(p, currentUserId))
                .sorted(Comparator.comparingInt(
                        (PinResponse p) -> p.getLikeCount() + p.getCommentCount() + p.getShareCount() + p.getSaveCount()
                ).reversed())
                .limit(limit)
                .toList();
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Page<PinResponse> getRelatedPins(Long pinId, int page, int size, Long currentUserId) {
//        Pin pin = findActivePin(pinId);
//        Pageable pageable = PageRequest.of(page, size);
//
//        // Priority 1: Same category
//        if (pin.getCategory() != null) {
//            Page<Pin> byCategory = pinRepository.findRelatedByCategory(
//                    pin.getCategory().getId(), pinId, pageable
//            );
//            if (byCategory.hasContent()) {
//                return byCategory.map(p -> toResponse(p, currentUserId));
//            }
//        }
//
//        if (pin.getTags() != null && !pin.getTags().isBlank()) {
//            String pattern = Arrays.stream(pin.getTags().split(","))
//                    .map(String::trim)
//                    .collect(Collectors.joining("|")); // regex OR pattern
//
//            Page<Pin> byTags = pinRepository.findRelatedByTagsNative(pinId, pattern, pageable);
//            if (byTags.hasContent()) {
//                return byTags.map(p -> toResponse(p, currentUserId));
//            }
//        }
//
//        // Fallback: Latest pins
//        return pinRepository
//                .findByDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(pageable)
//                .map(p -> toResponse(p, currentUserId));
//    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getRelatedPins(Long pinId, int page, int size, Long currentUserId) {
        Pin pin = findActivePin(pinId);
        Pageable pageable = PageRequest.of(page, size);
        var exclusions = feedExclusionService.getExclusionSet(currentUserId);

        // Priority 1: Same category
        if (pin.getCategory() != null) {
            List<Pin> byCategory = pinRepository.findRelatedByCategory(pin.getCategory().getId(), pinId, pageable)
                    .getContent().stream()
                    .filter(p -> !exclusions.excludes(p))
                    .toList();
            if (!byCategory.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(
                        byCategory.stream().map(p -> toResponse(p, currentUserId)).toList(), pageable, byCategory.size());
            }
        }

        // Priority 2: Matching tags
        if (pin.getTags() != null && !pin.getTags().isBlank()) {
            String pattern = Arrays.stream(pin.getTags().split(","))
                    .map(String::trim)
                    .collect(Collectors.joining("|")); // regex OR pattern

            List<Pin> byTags = pinRepository.findRelatedByTagsNative(pinId, pattern, pageable)
                    .getContent().stream()
                    .filter(p -> !exclusions.excludes(p))
                    .toList();
            if (!byTags.isEmpty()) {
                return new org.springframework.data.domain.PageImpl<>(
                        byTags.stream().map(p -> toResponse(p, currentUserId)).toList(), pageable, byTags.size());
            }
        }

        // Fallback: Latest pins
        List<Pin> fallback = pinRepository
                .findByDeletedFalseAndAlbumOnlyFalseOrderByCreatedAtDesc(pageable)
                .getContent().stream()
                .filter(p -> !exclusions.excludes(p))
                .toList();

        return new org.springframework.data.domain.PageImpl<>(
                fallback.stream().map(p -> toResponse(p, currentUserId)).toList(), pageable, fallback.size());
    }

    // ─── Download Pin ─────────────────────────────────────────────────────────
    @Override
    @Transactional
    public DownloadResponse trackPinDownload(Long pinId, Long currentUserId) throws IOException{
        Pin pin = findActivePin(pinId);
        User user = currentUserId != null
                ? userRepository.findById(currentUserId).orElse(null)
                : null;

        java.net.URL url = new java.net.URL(pin.getImageUrl());
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();   // ← can throw IOException
        connection.disconnect();

        if (responseCode != 200) {
            throw new IOException("Image unavailable for pin " + pinId + " (HTTP " + responseCode + ")");
        }

        pinDownloadRepository.save(new PinDownload(pin, user));

        if (user != null && !pin.getUser().getId().equals(currentUserId)) {
            notificationService.create(
                    pin.getUser(),
                    user,
                    "DOWNLOAD",
                    user.getFullName() + " downloaded your pin \"" + pin.getTitle() + "\"",
                    "/pin/" + pinId
            );
        }

        return DownloadResponse.builder()
                .id(pinId)
                .downloadUrl(pin.getImageUrl())
                .totalDownloads(pinDownloadRepository.countByPinId(pinId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getMostSavedPins(int limit, Long currentUserId) {
        return pinRepository.findMostSaved(PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getMostSharedPins(int limit, Long currentUserId) {
        return pinRepository.findMostShared(PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getMostDownloadedPins(int limit, Long currentUserId) {
        return pinRepository.findMostDownloaded(PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getMostViewedPins(int limit, Long currentUserId) {
        return pinRepository.findMostViewed(PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getUserMostSavedPins(Long userId, int limit, Long currentUserId) {
        return pinRepository.findMostSavedByUser(userId, PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getUserMostSharedPins(Long userId, int limit, Long currentUserId) {
        return pinRepository.findMostSharedByUser(userId, PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getUserMostDownloadedPins(Long userId, int limit, Long currentUserId) {
        return pinRepository.findMostDownloadedByUser(userId, PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PinResponse> getUserMostViewedPins(Long userId, int limit, Long currentUserId) {
        return pinRepository.findMostViewedByUser(userId, PageRequest.of(0, limit))
                .stream().map(p -> toResponse(p, currentUserId)).toList();
    }


    // ─── Private helpers ──────────────────────────────────────────────────

    private Pin findActivePin(Long pinId) {
        Pin pin = pinRepository.findById(pinId)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new RuntimeException("Pin not found"));
        if (pin.isSuspended())
            throw new RuntimeException("This pin has been suspended. Reason: " + pin.getSuspensionReason());

        return pin;
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
        r.setLikedByCurrentUser(
                currentUserId != null &&
                        likeRepository.existsByUserIdAndPinId(currentUserId, pin.getId())
        );
        r.setSuspended(pin.isSuspended());
        r.setSuspensionReason(pin.isSuspended() ? pin.getSuspensionReason() : null);
        r.setSaveCount((int) savedPinRepository.countByPinId(pin.getId()));
        r.setShareCount((int) pinShareRepository.countDistinctSharersByPinId(pin.getId()));
        r.setSavedByCurrentUser(
                currentUserId != null &&
                        savedPinRepository.existsByUserIdAndPinId(currentUserId, pin.getId())
        );
        r.setDownloadCount((int) pinDownloadRepository.countByPinId(pin.getId()));
        r.setViewCount(pin.getViewCount());

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
