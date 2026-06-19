package com.example.photoGroupe.controller.pin;

import com.example.photoGroupe.dto.pins.*;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.upload.PinsService;
import com.example.photoGroupe.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/pins")
@RequiredArgsConstructor
public class PinController {

    private final PinsService pinsService;
    private final UserService userService;

    // ─── Create ───────────────────────────────────────────────────────────

    // POST /api/pins   (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PinResponse> createPin(
            @RequestPart("image") MultipartFile image,
            @RequestPart(value = "title",       required = false) String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "tags",        required = false) String tags,
            @RequestPart(value = "categoryId",  required = false) String categoryId,  // add
            @RequestPart(value = "categorySlug", required = false) String categorySlug,  // add
            @RequestPart(value = "categoryName", required = false) String categoryName,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {

        PinRequest req = new PinRequest();
        req.setImage(image);
        req.setTitle(title);
        req.setDescription(description);
        req.setTags(tags);
        if (categoryId != null) req.setCategoryId(Long.parseLong(categoryId));        // add
        if (categorySlug != null) req.setCategorySlug(categorySlug);
        if (categoryName != null) req.setCategoryName(categoryName);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pinsService.createPin(req, currentUser.getId()));
    }

    // ─── Read ─────────────────────────────────────────────────────────────

    // GET /api/pins/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PinResponse> getPin(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getPin(id, userId));
    }

    @GetMapping("/{pinId}/related")
    public ResponseEntity<Page<PinResponse>> getRelatedPins(
            @PathVariable Long pinId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getRelatedPins(pinId, page, size, currentUserId));
    }

    // GET /api/pins?page=0&size=20          — home feed
    @GetMapping
    public ResponseEntity<Page<PinResponse>> getFeed(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getFeed(page, size, userId));
    }

    // GET /api/pins/user/{userId}?page=0&size=20   — profile grid
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PinResponse>> getUserPins(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long currentId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getUserPins(userId, page, size, currentId));
    }

    // GET /api/pins/search?tag=sunset&page=0&size=20
    @GetMapping("/search")
    public ResponseEntity<Page<PinResponse>> searchByTag(
            @RequestParam String tag,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.searchByTag(tag, page, size, userId));
    }

    // ─── Update ───────────────────────────────────────────────────────────

    // PUT /api/pins/{id}   (multipart/form-data, image optional)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PinResponse> updatePin(
            @PathVariable Long id,
            @RequestPart(value = "image",       required = false) MultipartFile image,
            @RequestPart(value = "title",       required = false) String title,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "tags",        required = false) String tags,
            @RequestPart(value = "categoryId",  required = false) String categoryId,  // add
            @RequestPart(value = "categorySlug", required = false) String categorySlug,
            @RequestPart(value = "categoryName", required = false) String categoryName,

            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {

        PinRequest req = new PinRequest();
        req.setImage(image);
        req.setTitle(title);
        req.setDescription(description);
        req.setTags(tags);
        if (categoryId != null) req.setCategoryId(Long.parseLong(categoryId));        // add
        if (categorySlug != null) req.setCategorySlug(categorySlug);
        if (categoryName != null) req.setCategoryName(categoryName);

        return ResponseEntity.ok(pinsService.updatePin(id, req, currentUser.getId()));
    }

    // ─── Delete ───────────────────────────────────────────────────────────

    // DELETE /api/pins/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePin(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {
        pinsService.deletePin(id, currentUser.getId(), currentUser.getUser().getRole());
        return ResponseEntity.noContent().build();
    }

    // ─── Likes ────────────────────────────────────────────────────────────

    // POST /api/pins/{id}/like   — toggles like/unlike
    @PostMapping("/{id}/like")
    public ResponseEntity<PinResponse> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(pinsService.toggleLike(id, currentUser.getId()));
    }

    // ─── Comments ─────────────────────────────────────────────────────────

    // POST /api/pins/{id}/comments
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pinsService.addComment(id, request, currentUser.getId()));
    }

    // GET /api/pins/{id}/comments?page=0&size=20
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getComments(id, page, size, userId));
    }

    // DELETE /api/pins/{pinId}/comments/{commentId}
    @DeleteMapping("/{pinId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long pinId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        pinsService.deleteComment(commentId, currentUser.getId(), currentUser.getUser().getRole());
        return ResponseEntity.noContent().build();
    }

    // GET /api/users/pins/category?slug=landscape-photography
    @GetMapping("/category")
    public ResponseEntity<Page<PinResponse>> getPinsByCategorySlug(
            @RequestParam String slug,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getPinsByCategorySlug(slug, page, size, currentUserId));
    }

    @GetMapping("/category/name")
    public ResponseEntity<Page<PinResponse>> getPinsByCategoryName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(pinsService.getPinsByCategoryName(name, page, size, currentUserId));
    }

    // Update comment
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(pinsService.updateComment(commentId, request, currentUser.getId()));
    }

    // Like / unlike comment
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<CommentResponse> toggleCommentLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(pinsService.toggleCommentLike(commentId, currentUser.getId()));
    }

    // Reply to comment
    @PostMapping("/{pinId}/comments/{parentCommentId}/reply")
    public ResponseEntity<CommentResponse> replyToComment(
            @PathVariable Long pinId,
            @PathVariable Long parentCommentId,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(pinsService.replyToComment(pinId, parentCommentId, request, currentUser.getId()));
    }

    @PutMapping("/{pinId}/suspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PinResponse> suspendPin(
            @PathVariable Long pinId,
            @RequestBody PinSuspensionRequest req,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(pinsService.suspendPin(pinId, req.getReason(), currentUser.getUser()));
    }

    @PutMapping("/{pinId}/unsuspend")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<PinResponse> unsuspendPin(
            @PathVariable Long pinId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(pinsService.unsuspendPin(pinId, currentUser.getUser()));
    }
    // POST /api/pins/{pinId}/share
    @PostMapping("/{pinId}/share")
    public ResponseEntity<SharePinResponse> sharePin(
            @PathVariable Long pinId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(pinsService.sharePin(pinId, currentUser.getId()));
    }

    // POST /api/pins/{pinId}/save  (toggle)
    @PostMapping("/{pinId}/save")
    public ResponseEntity<SavePinResponse> toggleSavePin(
            @PathVariable Long pinId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(pinsService.toggleSavePin(pinId, currentUser.getId()));
    }

    // GET /api/pins/saved
    @GetMapping("/saved")
    public ResponseEntity<Page<PinResponse>> getSavedPins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        return ResponseEntity.ok(pinsService.getSavedPins(page, size, currentUser.getId()));
    }

    // GET /api/pins/{pinId}/shares/count
    @GetMapping("/{pinId}/shares/count")
    public ResponseEntity<Map<String, Long>> getShareCount(@PathVariable Long pinId) {
        return ResponseEntity.ok(Map.of("shareCount", pinsService.getShareCount(pinId)));
    }

    @GetMapping("/top-pins")
    public ResponseEntity<List<PinResponse>> getTopPins(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(pinsService.getTopPins(limit, userDetails.getUser().getId()));
    }

    @GetMapping("/{userId}/top-pins")
    public ResponseEntity<List<PinResponse>> getTopPinsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long currentUserId = userDetails.getUser().getId();
        return ResponseEntity.ok(pinsService.getTopPinsByUser(userId, limit, currentUserId));
    }
}
