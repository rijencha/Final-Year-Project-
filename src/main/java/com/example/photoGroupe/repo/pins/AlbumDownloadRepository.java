package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.AlbumDownload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumDownloadRepository extends JpaRepository<AlbumDownload, Long> {
    long countByAlbumId(Long albumId);
    @Query("SELECT COUNT(ad) FROM AlbumDownload ad WHERE ad.album.user.id = :userId")
    long countByAlbumUserId(@Param("userId") Long userId);
}