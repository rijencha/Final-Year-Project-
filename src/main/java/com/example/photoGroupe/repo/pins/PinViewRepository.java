package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.PinView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PinViewRepository extends JpaRepository<PinView, Long> {
    Optional<PinView> findByPinIdAndUserId(Long pinId, Long userId);
}