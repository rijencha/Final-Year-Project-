package com.example.photoGroupe.service.photographer;

import com.example.photoGroupe.dto.eventandbid.EventTypeOptionsResponse;
import com.example.photoGroupe.dto.eventandbid.SpecializationRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.exception.ResourceNotFoundException;
import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.model.event.PhotographerSpecialization;
import com.example.photoGroupe.model.rating.PhotographerReview;
import com.example.photoGroupe.repo.CategoryRepository;
import com.example.photoGroupe.repo.PhotographerReviewRepository;
import com.example.photoGroupe.repo.PhotographerSpecializationRepository;
import com.example.photoGroupe.repo.PinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PhotographerProfileServiceImpl implements PhotographerProfileService {

    private final PhotographerSpecializationRepository specializationRepo;
    private final CategoryRepository categoryRepository;
    private final PhotographerReviewRepository  reviewRepository;
    private final PinRepository pinRepository;

    @Override
    public List<SpecializationResponse> getSpecializations(Long photographerId) {
        return specializationRepo.findAllByPhotographerId(photographerId)
                .stream().map(SpecializationResponse::from).toList();
    }

    @Override
    @Transactional
    public SpecializationResponse addSpecialization(User photographer, SpecializationRequest req) {
        if (req.getCustomType() == null || req.getCustomType().isBlank())
            throw new RuntimeException("customType is required");

        String normalised = req.getCustomType().trim();

        if (specializationRepo.existsByPhotographerIdAndCustomTypeIgnoreCase(
                photographer.getId(), normalised))
            throw new RuntimeException("Already specialized in " + normalised);

        // ── Resolve category if provided ────────────────────────────────
        Category category = null;
        if (req.getCategoryId() != null) {
            category = categoryRepository.findByIdAndDeletedFalse(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found: " + req.getCategoryId()));
        }
        // ───────────────────────────────────────────────────────────────

        PhotographerSpecialization s = PhotographerSpecialization.builder()
                .photographer(photographer)
                .customType(normalised)
                .note(req.getNote())
                .category(category)          // ← set the link
                .build();

        return SpecializationResponse.from(specializationRepo.save(s));
    }

    @Override
    @Transactional
    public void removeCustomSpecialization(User photographer, Long id) {
        PhotographerSpecialization s = specializationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization not found"));
        if (!s.getPhotographer().getId().equals(photographer.getId()))
            throw new RuntimeException("Not authorized");
        specializationRepo.delete(s);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotographerDetail> getPhotographersByCategory(String keyword) {
        List<User> byName = specializationRepo.findPhotographersByCategoryName(keyword);
        List<User> users = byName.isEmpty()
                ? specializationRepo.findPhotographersByCustomType(keyword)
                : byName;

        return users.stream()
                .map(this::toPhotographerDetail)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EventTypeOptionsResponse getEventTypeOptions() {
        List<String> standard = Arrays.stream(EventType.values())
                .map(Enum::name)
                .toList();  // keeps UPPERCASE

        List<String> custom = specializationRepo.findAllDistinctCustomTypes();

        // Exclude custom entries that duplicate a standard enum (case-insensitive)
        List<String> filteredCustom = custom.stream()
                .filter(c -> standard.stream()
                        .noneMatch(s -> s.equalsIgnoreCase(c)))
                .toList();

        List<String> all = Stream.concat(standard.stream(), filteredCustom.stream())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        return EventTypeOptionsResponse.builder()
                .standard(standard)
                .custom(custom)         // raw custom list unchanged
                .all(all)               // standard UPPERCASE + unique custom mixed
                .build();
    }

    private PhotographerDetail toPhotographerDetail(User user) {
        List<PhotographerReview> reviews =
                reviewRepository.findByPhotographerIdAndDeletedFalseOrderByCreatedAtDesc(user.getId());

        double avg = reviews.stream()
                .filter(r -> r.getRating() > 0)
                .mapToInt(PhotographerReview::getRating)
                .average()
                .orElse(0.0);
        long ratingCount = reviews.stream()
                .filter(r -> r.getRating() > 0)
                .count();

        long reviewCount = reviews.stream()
                .filter(r -> r.getComment() != null && !r.getComment().isBlank())
                .count();
        List<SpecializationResponse> specializations = specializationRepo  // ← add this
                .findAllByPhotographerId(user.getId())
                .stream()
                .map(SpecializationResponse::from)
                .toList();
        return PhotographerDetail.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getActualUsername())
                .phoneNumber(user.getPhoneNumber())
                .bio(user.getBio())
                .location(user.getLocation())
                .profilePicture(user.getProfilePicture())
                .pinCount(pinRepository.countByUserId(user.getId()))
                .verified(user.isVerified())
                .enable(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .deleted(user.isDeleted())
                .joinedAt(user.getCreatedAt())
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .ratingCount(ratingCount)
                .reviewCount(reviewCount)
                //            .recentReviews(reviewResponses)
                .yearsOfExperience(user.getYearsOfExperience())       // ← add this
                .specializations(specializations)
                .build();
    }

}
