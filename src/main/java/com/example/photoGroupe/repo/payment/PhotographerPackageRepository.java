package com.example.photoGroupe.repo.payment;

import com.example.photoGroupe.model.booking.PhotographerPackage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotographerPackageRepository extends JpaRepository<PhotographerPackage, Long> {
    List<PhotographerPackage> findAllByPhotographerId(Long photographerId);
}