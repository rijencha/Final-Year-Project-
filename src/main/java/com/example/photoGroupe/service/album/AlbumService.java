package com.example.photoGroupe.service.album;

import com.example.photoGroupe.dto.album.AlbumRequest;
import com.example.photoGroupe.dto.album.AlbumResponse;
import com.example.photoGroupe.dto.album.AlbumZipPayload;
import com.example.photoGroupe.dto.album.DownloadResponse;
import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.dto.share.ShareResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AlbumService {
    AlbumResponse createAlbum(AlbumRequest request, Long currentUserId) throws IOException;
    AlbumResponse getAlbum(Long albumId, Long currentUserId);
    AlbumResponse updateAlbum(Long albumId, AlbumRequest request, Long currentUserId) throws IOException;
    void deleteAlbum(Long albumId, Long currentUserId) throws IOException;
    AlbumResponse uploadPinToAlbum(Long albumId, List<MultipartFile> image, String title, String description, Long currentUserId) throws IOException;

    AlbumResponse addPinToAlbum(Long albumId, List<Long> pinId, Long currentUserId);
    AlbumResponse removePinFromAlbum(Long albumId, Long pinId, Long currentUserId);

    Page<AlbumResponse> getUserAlbums(Long userId, int page, int size, Long currentUserId);
    Page<AlbumResponse> getPublicAlbums(int page, int size);
    Page<PinResponse> getAlbumPins(Long albumId, int page, int size, Long currentUserId);

    // Download tracking — returns Cloudinary URL + increments counter
    DownloadResponse trackAlbumDownload(Long albumId, Long currentUserId);
    AlbumZipPayload prepareAlbumZip(Long albumId, Long currentUserId);
    long getUserTotalDownloads(Long userId);
    ShareResponse shareAlbum(Long albumId, Long currentUserId);


}
