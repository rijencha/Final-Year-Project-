package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.pins.SavedPin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavedPinRepository extends JpaRepository<SavedPin, Long> {
    Optional<SavedPin> findByUserIdAndPinId(Long userId, Long pinId);

    boolean existsByUserIdAndPinId(Long userId, Long pinId);

    Page<SavedPin> findByUserIdOrderBySavedAtDesc(Long userId, Pageable pageable);

    long countByPinId(Long pinId);
}
