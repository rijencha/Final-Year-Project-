package com.example.photoGroupe.controller.pin;


import com.example.photoGroupe.dto.album.AlbumRequest;
import com.example.photoGroupe.dto.album.AlbumResponse;
import com.example.photoGroupe.dto.album.AlbumZipPayload;
import com.example.photoGroupe.dto.album.DownloadResponse;
import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.album.AlbumService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/users/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    // ─── CRUD ─────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> createAlbum(
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibility", required = false) String visibility,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {

        AlbumRequest request = new AlbumRequest();
        request.setTitle(title);
        request.setDescription(description);
        if (visibility != null) request.setVisibility(visibility);
        request.setCoverImage(coverImage);

        return ResponseEntity.ok(albumService.createAlbum(request, currentUser.getId()));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumResponse> getAlbum(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(albumService.getAlbum(albumId, userId));
    }

    @PutMapping(value = "/{albumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> updateAlbum(
            @PathVariable Long albumId,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "visibility", required = false) String visibility,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {

        AlbumRequest request = new AlbumRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setVisibility(visibility);
        request.setCoverImage(coverImage);

        return ResponseEntity.ok(albumService.updateAlbum(albumId, request, currentUser.getId()));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {
        albumService.deleteAlbum(albumId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
    // ─── Pin management ───────────────────────────────────────────────────

    @PostMapping("/{albumId}/pins")
    public ResponseEntity<AlbumResponse> addPins(
            @PathVariable Long albumId,
            @RequestBody List<Long> pinIds,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(albumService.addPinToAlbum(albumId, pinIds, currentUser.getId()));
    }

    @DeleteMapping("/{albumId}/pins/{pinId}")
    public ResponseEntity<AlbumResponse> removePin(
            @PathVariable Long albumId,
            @PathVariable Long pinId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(albumService.removePinFromAlbum(albumId, pinId, currentUser.getId()));
    }

    @GetMapping("/{albumId}/pins")
    public ResponseEntity<Page<PinResponse>> getAlbumPins(
            @PathVariable Long albumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(albumService.getAlbumPins(albumId, page, size, userId));
    }

    // ─── Browse ───────────────────────────────────────────────────────────

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<AlbumResponse>> getUserAlbums(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Long cuid = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(albumService.getUserAlbums(userId, page, size, cuid));
    }

    @GetMapping("/public")
    public ResponseEntity<Page<AlbumResponse>> getPublicAlbums(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(albumService.getPublicAlbums(page, size));
    }

    // ─── Downloads ────────────────────────────────────────────────────────

    @PostMapping("/{albumId}/download")
    public void downloadAlbumZip(
            @PathVariable Long albumId,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletResponse response) throws IOException {

        Long userId = currentUser != null ? currentUser.getId() : null;
        AlbumZipPayload payload = albumService.prepareAlbumZip(albumId, userId);

        response.setContentType("application/zip");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=\"" + sanitizeFileName(payload.albumTitle()) + ".zip\""
        );

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            int index = 1;
            for (AlbumZipPayload.AlbumZipItem item : payload.items()) {
                String extension = extractExtension(item.imageUrl());
                String baseName = sanitizeFileName(item.title() != null ? item.title() : "pin");
                String entryName = String.format("%02d_%s%s", index++, baseName, extension);

                try (InputStream in = URI.create(item.imageUrl()).toURL().openStream()) {
                    zos.putNextEntry(new ZipEntry(entryName));
                    in.transferTo(zos);
                    zos.closeEntry();
                } catch (IOException ex) {
                    // one image failed to fetch — skip it, keep zipping the rest
                }
            }
            zos.finish();
        }
    }

    @PostMapping(value = "/{albumId}/pins/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AlbumResponse> uploadPinsToAlbum(
            @PathVariable Long albumId,
            @RequestPart("images") List<MultipartFile> images,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) throws IOException {
        return ResponseEntity.ok(albumService.uploadPinToAlbum(albumId, images, title, description, currentUser.getId()));
    }

    @GetMapping("/{userId}/download-stats")
    public ResponseEntity<Long> getUserTotalDownloads(@PathVariable Long userId) {
        return ResponseEntity.ok(albumService.getUserTotalDownloads(userId));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private String sanitizeFileName(String name) {
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private String extractExtension(String url) {
        try {
            String path = URI.create(url).toURL().getPath();
            int dot = path.lastIndexOf('.');
            if (dot >= 0 && dot < path.length() - 1) {
                String ext = path.substring(dot);
                if (ext.length() <= 5) return ext; // guard against garbage "extensions"
            }
        } catch (Exception ignored) { /* fall through to default */ }
        return ".jpg";
    }

}
