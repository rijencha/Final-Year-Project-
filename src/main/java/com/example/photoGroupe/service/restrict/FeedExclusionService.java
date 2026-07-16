package com.example.photoGroupe.service.restrict;

import com.example.photoGroupe.dto.restrict.FeedExclusionDtos.FeedExclusionRequest;
import com.example.photoGroupe.dto.restrict.FeedExclusionDtos.FeedExclusionResponse;
import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.Pin;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.restrict.FeedExclusion;
import com.example.photoGroupe.model.restrict.FeedExclusionScope;
import com.example.photoGroupe.repo.CategoryRepository;
import com.example.photoGroupe.repo.PinRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.restrict.FeedExclusionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedExclusionService {

    private final FeedExclusionRepository exclusionRepository;
    private final UserRepository userRepository;
    private final PinRepository pinRepository;
    private final CategoryRepository categoryRepository;

    public void exclude(Long ownerId, FeedExclusionRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        switch (request.getScope()) {
            case PIN -> {
                if (exclusionRepository.findByOwnerIdAndScopeAndPinId(ownerId, FeedExclusionScope.PIN, request.getPinId()).isPresent())
                    return; // already excluded, no-op
                Pin pin = pinRepository.findById(request.getPinId())
                        .orElseThrow(() -> new RuntimeException("Pin not found"));
                exclusionRepository.save(new FeedExclusion(owner, FeedExclusionScope.PIN, pin, null, null));
            }
            case USER -> {
                if (exclusionRepository.findByOwnerIdAndScopeAndExcludedUserId(ownerId, FeedExclusionScope.USER, request.getExcludedUserId()).isPresent())
                    return;
                User excludedUser = userRepository.findById(request.getExcludedUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                exclusionRepository.save(new FeedExclusion(owner, FeedExclusionScope.USER, null, excludedUser, null));
            }
            case CATEGORY -> {
                if (exclusionRepository.findByOwnerIdAndScopeAndCategoryId(ownerId, FeedExclusionScope.CATEGORY, request.getCategoryId()).isPresent())
                    return;
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Category not found"));
                exclusionRepository.save(new FeedExclusion(owner, FeedExclusionScope.CATEGORY, null, null, category));
            }
        }
    }

    public void undoExclusion(Long ownerId, FeedExclusionScope scope, Long targetId) {
        Optional<FeedExclusion> existing = switch (scope) {
            case PIN -> exclusionRepository.findByOwnerIdAndScopeAndPinId(ownerId, scope, targetId);
            case USER -> exclusionRepository.findByOwnerIdAndScopeAndExcludedUserId(ownerId, scope, targetId);
            case CATEGORY -> exclusionRepository.findByOwnerIdAndScopeAndCategoryId(ownerId, scope, targetId);
        };
        existing.ifPresent(f -> exclusionRepository.deleteById(f.getId()));
    }

    public List<FeedExclusionResponse> getMyExclusions(Long ownerId) {
        return exclusionRepository.findAllByOwnerId(ownerId).stream()
                .map(f -> {
                    Pin pin = f.getPin();
                    User excludedUser = f.getExcludedUser();
                    Category category = f.getCategory();

                    return new FeedExclusionResponse(
                            f.getId(),
                            f.getScope(),

                            pin != null ? pin.getId() : null,
                            pin != null ? pin.getTitle() : null,
                            pin != null ? pin.getImageUrl() : null,
                            pin != null ? pin.getUser().getId() : null,
                            pin != null ? pin.getUser().getFullName() : null,

                            excludedUser != null ? excludedUser.getId() : null,
                            excludedUser != null ? excludedUser.getActualUsername() : null,
                            excludedUser != null ? excludedUser.getFullName() : null,
                            excludedUser != null ? excludedUser.getProfilePicture() : null,

                            category != null ? category.getId() : null,
                            category != null ? category.getName() : null,
                            category != null ? category.getCoverImage() : null,

                            f.getCreatedAt()
                    );
                })
                .toList();
    }

    /** Bundles the three exclusion sets for one user — call once per feed request, not per pin. */
    public ExclusionSet getExclusionSet(Long ownerId) {
        return new ExclusionSet(
                Set.copyOf(exclusionRepository.findExcludedPinIds(ownerId)),
                Set.copyOf(exclusionRepository.findExcludedUserIds(ownerId)),
                Set.copyOf(exclusionRepository.findExcludedCategoryIds(ownerId))
        );
    }

    public record ExclusionSet(Set<Long> pinIds, Set<Long> userIds, Set<Long> categoryIds) {
        public boolean excludes(Pin pin) {
            if (pinIds.contains(pin.getId())) return true;
            if (userIds.contains(pin.getUser().getId())) return true;
            return pin.getCategory() != null && categoryIds.contains(pin.getCategory().getId());
        }
    }
}