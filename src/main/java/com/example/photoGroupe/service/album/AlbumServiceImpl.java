package com.example.photoGroupe.service.album;

import com.cloudinary.Cloudinary;
import com.example.photoGroupe.dto.album.AlbumRequest;
import com.example.photoGroupe.dto.album.AlbumResponse;
import com.example.photoGroupe.dto.album.AlbumZipPayload;
import com.example.photoGroupe.dto.album.DownloadResponse;
import com.example.photoGroupe.dto.pins.PinRequest;
import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.pins.Album;
import com.example.photoGroupe.model.pins.AlbumDownload;
import com.example.photoGroupe.model.pins.AlbumPin;
import com.example.photoGroupe.model.pins.PinDownload;
import com.example.photoGroupe.repo.*;
import com.example.photoGroupe.repo.pins.AlbumDownloadRepository;
import com.example.photoGroupe.repo.pins.AlbumPinRepository;
import com.example.photoGroupe.repo.pins.AlbumRepository;
import com.example.photoGroupe.repo.pins.PinDownloadRepository;
import com.example.photoGroupe.service.upload.CloudinaryService;
import com.example.photoGroupe.service.upload.PinsService;
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

@Service
@RequiredArgsConstructor
public class AlbumServiceImpl implements AlbumService{

    private final AlbumRepository albumRepository;
    private final AlbumPinRepository albumPinRepository;
    private final PinRepository pinRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final AlbumDownloadRepository albumDownloadRepository;
    private final PinLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final SavedPinRepository savedPinRepository;
    private final PinShareRepository pinShareRepository;
    private final PinsService pinsService;

    @Override
    @Transactional
    public AlbumResponse createAlbum(AlbumRequest request, Long currentUserId) throws IOException {
        User user = findUser(currentUserId);
        Album album = new Album(request.getTitle(), request.getDescription(),
                request.getVisibility(), user);

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            String[] upload = cloudinaryService.uploadAlbumCover(request.getCoverImage(), currentUserId);
            album.setCoverImageUrl(upload[0]);
            album.setCoverPublicId(upload[1]);
        }

        albumRepository.save(album);
        return toResponse(album);
    }

    @Override
    @Transactional(readOnly = true)
    public AlbumResponse getAlbum(Long albumId, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        assertVisibility(album, currentUserId);
        if (currentUserId == null || !currentUserId.equals(album.getUser().getId())) {
            album.setViewCount(album.getViewCount() + 1);
            albumRepository.save(album);
        }
        return toResponse(album);
    }

    @Override
    @Transactional
    public AlbumResponse updateAlbum(Long albumId, AlbumRequest request, Long currentUserId) throws IOException {
        Album album = findActiveAlbum(albumId);
        assertOwner(album, currentUserId);

        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            if (album.getCoverPublicId() != null) {
                cloudinaryService.deleteImage(album.getCoverPublicId());
            }
            String[] upload = cloudinaryService.uploadAlbumCover(request.getCoverImage(), currentUserId);
            album.setCoverImageUrl(upload[0]);
            album.setCoverPublicId(upload[1]);
        }

        if (request.getTitle()       != null) album.setTitle(request.getTitle());
        if (request.getDescription() != null) album.setDescription(request.getDescription());
        if (request.getVisibility()  != null) album.setVisibility(request.getVisibility());

        albumRepository.save(album);
        return toResponse(album);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId, Long currentUserId) throws IOException {
        Album album = findActiveAlbum(albumId);
        assertOwner(album, currentUserId);

        if (album.getCoverPublicId() != null) {
            cloudinaryService.deleteImage(album.getCoverPublicId());
        }

        album.setDeleted(true);
        albumRepository.save(album);
    }

    @Override
    @Transactional
    public AlbumResponse uploadPinToAlbum(Long albumId, List<MultipartFile> images, String title, String description, Long currentUserId) throws IOException {
        Album album = findActiveAlbum(albumId);
        assertOwner(album, currentUserId);

        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) continue;

            PinRequest pinRequest = new PinRequest();
            pinRequest.setImage(image);
            pinRequest.setTitle(title);             // same title/desc applied to every file in the batch
            pinRequest.setDescription(description);

            PinResponse createdPin = pinsService.createPin(pinRequest, currentUserId, true);

            Pin pin = pinRepository.findById(createdPin.getId())
                    .orElseThrow(() -> new RuntimeException("Pin creation failed"));

            albumPinRepository.save(new AlbumPin(album, pin));

            if (album.getCoverImageUrl() == null)
                album.setCoverImageUrl(pin.getImageUrl());
        }

        albumRepository.save(album);
        return toResponse(album);
    }

    // ─── Pin Management ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AlbumResponse addPinToAlbum(Long albumId, List<Long> pinIds, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        assertOwner(album, currentUserId);

        for (Long pinId : pinIds) {
            Pin pin = pinRepository.findById(pinId)
                    .filter(p -> !p.isDeleted())
                    .orElse(null);

            if (pin == null) continue;
            if (albumPinRepository.existsByAlbumIdAndPinId(albumId, pinId)) continue;

            albumPinRepository.save(new AlbumPin(album, pin));

            if (album.getCoverImageUrl() == null)
                album.setCoverImageUrl(pin.getImageUrl());
        }

        albumRepository.save(album);
        return toResponse(album);
    }

    @Override
    @Transactional
    public AlbumResponse removePinFromAlbum(Long albumId, Long pinId, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        assertOwner(album, currentUserId);

        AlbumPin albumPin = albumPinRepository.findByAlbumIdAndPinId(albumId, pinId)
                .orElseThrow(() -> new RuntimeException("Pin not in album"));
        albumPinRepository.delete(albumPin);

        return toResponse(album);
    }

    // ─── Queries ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AlbumResponse> getUserAlbums(Long userId, int page, int size, Long currentUserId) {
        Pageable pageable = PageRequest.of(page, size);
        boolean isOwner = currentUserId != null && currentUserId.equals(userId);

        Page<Album> albums = isOwner
                ? albumRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, pageable)
                : albumRepository.findByUserIdAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(userId, "PUBLIC", pageable);

        return albums.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlbumResponse> getPublicAlbums(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return albumRepository
                .findByDeletedFalseAndVisibilityOrderByCreatedAtDesc("PUBLIC", pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PinResponse> getAlbumPins(Long albumId, int page, int size, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        assertVisibility(album, currentUserId);
        Pageable pageable = PageRequest.of(page, size);
        return albumPinRepository
                .findByAlbumIdOrderByAddedAtDesc(albumId, pageable)
                .map(ap -> toPinResponse(ap.getPin(), currentUserId));
    }

    // ─── Downloads ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public DownloadResponse trackAlbumDownload(Long albumId, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        User user = currentUserId != null ? findUser(currentUserId) : null;
        albumDownloadRepository.save(new AlbumDownload(album, user));

        // Collect all pin image URLs in the album for the frontend to zip
        String pinUrlsCsv = album.getAlbumPins().stream()
                .map(ap -> ap.getPin().getImageUrl())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "," + b);

        return DownloadResponse.builder()
                .id(albumId)
                .downloadUrl(pinUrlsCsv)   // comma-separated list of image URLs
                .totalDownloads(albumDownloadRepository.countByAlbumId(albumId))
                .build();
    }

    @Override
    @Transactional
    public AlbumZipPayload prepareAlbumZip(Long albumId, Long currentUserId) {
        Album album = findActiveAlbum(albumId);
        assertVisibility(album, currentUserId);

        User user = currentUserId != null ? findUser(currentUserId) : null;
        albumDownloadRepository.save(new AlbumDownload(album, user));

        List<AlbumZipPayload.AlbumZipItem> items = album.getAlbumPins().stream()
                .map(AlbumPin::getPin)
                .filter(p -> p.getImageUrl() != null && !p.getImageUrl().isBlank())
                .map(p -> new AlbumZipPayload.AlbumZipItem(p.getImageUrl(), p.getTitle()))
                .collect(java.util.stream.Collectors.toList());

        return new AlbumZipPayload(album.getTitle(), items);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserTotalDownloads(Long userId) {
        return albumDownloadRepository.countByAlbumUserId(userId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private Album findActiveAlbum(Long albumId) {
        return albumRepository.findById(albumId)
                .filter(a -> !a.isDeleted())
                .orElseThrow(() -> new RuntimeException("Album not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void assertOwner(Album album, Long userId) {
        if (!album.getUser().getId().equals(userId))
            throw new AccessDeniedException("Not authorized");
    }

    private void assertVisibility(Album album, Long currentUserId) {
        if ("PRIVATE".equals(album.getVisibility()) &&
                !album.getUser().getId().equals(currentUserId))
            throw new AccessDeniedException("This album is private");
    }

    // ─── Mapper ───────────────────────────────────────────────────────────

    private AlbumResponse toResponse(Album album) {
        AlbumResponse r = new AlbumResponse();
        r.setId(album.getId());
        r.setTitle(album.getTitle());
        r.setDescription(album.getDescription());
        r.setVisibility(album.getVisibility());
        r.setCoverImageUrl(album.getCoverImageUrl());
        r.setAuthorId(album.getUser().getId());
        r.setAuthorUsername(album.getUser().getActualUsername());
        r.setAuthorProfilePicture(album.getUser().getProfilePicture());
        r.setPinCount((int) albumPinRepository.countByAlbumId(album.getId()));
        r.setDownloadCount(albumDownloadRepository.countByAlbumId(album.getId()));
        r.setCreatedAt(album.getCreatedAt());
        r.setUpdatedAt(album.getUpdatedAt());
        r.setViewCount(album.getViewCount());
        return r;
    }

    private PinResponse toPinResponse(Pin pin, Long currentUserId) {
        PinResponse r = new PinResponse();
        r.setId(pin.getId());
        r.setImageUrl(pin.getImageUrl());
        r.setTitle(pin.getTitle());
        r.setDescription(pin.getDescription());
        r.setTags(pin.getTags());
        r.setCreatedAt(pin.getCreatedAt());
        r.setAuthorId(pin.getUser().getId());
        r.setAuthorUsername(pin.getUser().getActualUsername());
        r.setAuthorProfilePicture(pin.getUser().getProfilePicture());
        r.setAuthorName(pin.getUser().getFullName());          // ← add this
        r.setAuthorRole(pin.getUser().getRole().name());        // ← add this
        r.setLikeCount((int) likeRepository.countByPinId(pin.getId()));
        r.setCommentCount((int) commentRepository.countByPinIdAndDeletedFalse(pin.getId()));
        r.setLikedByCurrentUser(currentUserId != null &&
                likeRepository.existsByUserIdAndPinId(currentUserId, pin.getId()));
        r.setSaveCount((int) savedPinRepository.countByPinId(pin.getId()));
        r.setShareCount((int) pinShareRepository.countDistinctSharersByPinId(pin.getId()));
        r.setSavedByCurrentUser(currentUserId != null &&
                savedPinRepository.existsByUserIdAndPinId(currentUserId, pin.getId()));
        r.setSuspended(pin.isSuspended());
        r.setCategoryId(pin.getCategory() != null ? pin.getCategory().getId() : null);       // ← add this too
        r.setCategoryName(pin.getCategory() != null ? pin.getCategory().getName() : null);
        r.setCategorySlug(pin.getCategory() != null ? pin.getCategory().getSlug() : null);
        r.setViewCount(pin.getViewCount());
        return r;
    }
}
