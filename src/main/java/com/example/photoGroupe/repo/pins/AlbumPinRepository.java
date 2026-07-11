package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.AlbumPin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlbumPinRepository extends JpaRepository<AlbumPin, Long> {
    Optional<AlbumPin> findByAlbumIdAndPinId(Long albumId, Long pinId);
    Page<AlbumPin> findByAlbumIdOrderByAddedAtDesc(Long albumId, Pageable pageable);
    boolean existsByAlbumIdAndPinId(Long albumId, Long pinId);
    long countByAlbumId(Long albumId);
}
