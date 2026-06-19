package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.pins.PinShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PinShareRepository extends JpaRepository<PinShare, Long> {

    long countByPinId(Long pinId);
    @Query("SELECT COUNT(DISTINCT ps.sharedBy.id) FROM PinShare ps WHERE ps.pin.id = :pinId")
    long countDistinctSharersByPinId(@Param("pinId") Long pinId);
}
