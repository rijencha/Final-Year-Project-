package com.example.photoGroupe.service.booking;

import com.example.photoGroupe.dto.booking.PhotographerPackageRequest;
import com.example.photoGroupe.dto.booking.PhotographerPackageResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.booking.PhotographerPackage;
import com.example.photoGroupe.repo.payment.PhotographerPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotographerPackageService {

    private final PhotographerPackageRepository templateRepository;

    // ── Create template ──────────────────────────────────────────────────
    public PhotographerPackageResponse create(PhotographerPackageRequest req, User photographer) {
        PhotographerPackage template = PhotographerPackage.builder()
                .photographer(photographer)
                .name(req.getName())
                .packageType(req.getPackageType())
                .price(req.getPrice())
                .description(req.getDescription())
                .deliveryDays(req.getDeliveryDays())
                .build();
        return PhotographerPackageResponse.from(templateRepository.save(template));
    }

    // ── Update template ──────────────────────────────────────────────────
    public PhotographerPackageResponse update(Long id, PhotographerPackageRequest req,
                                              User photographer) {
        PhotographerPackage template = findAndValidate(id, photographer.getId());
        template.setName(req.getName());
        template.setPrice(req.getPrice());
        template.setDescription(req.getDescription());
        template.setDeliveryDays(req.getDeliveryDays());
        return PhotographerPackageResponse.from(templateRepository.save(template));
    }

    // ── Delete template ──────────────────────────────────────────────────
    public void delete(Long id, User photographer) {
        PhotographerPackage template = findAndValidate(id, photographer.getId());
        templateRepository.delete(template);
    }

    // ── Get my templates ─────────────────────────────────────────────────
    public List<PhotographerPackageResponse> getMyTemplates(User photographer) {
        return templateRepository.findAllByPhotographerId(photographer.getId())
                .stream()
                .map(PhotographerPackageResponse::from)
                .toList();
    }

    // ── Get templates by photographer ID (public — clients can browse) ───
    public List<PhotographerPackageResponse> getByPhotographerId(Long photographerId) {
        return templateRepository.findAllByPhotographerId(photographerId)
                .stream()
                .map(PhotographerPackageResponse::from)
                .toList();
    }

    public PhotographerPackage findById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package template not found"));
    }

    // ── Helper ───────────────────────────────────────────────────────────
    private PhotographerPackage findAndValidate(Long id, Long photographerId) {
        PhotographerPackage template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package template not found"));
        if (!template.getPhotographer().getId().equals(photographerId))
            throw new RuntimeException("Not authorized");
        return template;
    }
}