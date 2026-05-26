package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.PinLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PinLikeRepository extends JpaRepository<PinLike, Long> {

    Optional<PinLike> findByUserIdAndPinId(Long userId, Long pinId);

    boolean existsByUserIdAndPinId(Long userId, Long pinId);

    // Count likes for a pin directly (faster than loading the collection)
    long countByPinId(Long pinId);
}
