package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.pins.PinShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinShareRepository extends JpaRepository<PinShare, Long> {

    long countByPinId(Long pinId);
}
