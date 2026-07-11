package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.Album;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumRepository extends JpaRepository<Album, Long> {
    Page<Album> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<Album> findByDeletedFalseAndVisibilityOrderByCreatedAtDesc(String visibility, Pageable pageable);
    // repo/pins/AlbumRepository.java
    Page<Album> findByUserIdAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(Long userId, String visibility, Pageable pageable);
}