package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.PinDownload;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PinDownloadRepository extends JpaRepository<PinDownload, Long> {
    long countByPinId(Long pinId);
}
