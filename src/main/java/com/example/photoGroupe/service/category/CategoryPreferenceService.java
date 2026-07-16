package com.example.photoGroupe.service.category;

import com.example.photoGroupe.dto.category.CategoryPreferenceDtos.*;
import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.pins.CategoryPreference;
import com.example.photoGroupe.repo.CategoryRepository;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.pins.CategoryPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class CategoryPreferenceService {

    // ─── Tunable constants ──────────────────────────────────────────────
    public static final double DEFAULT_WEIGHT  = 1.0;
    public static final double MIN_WEIGHT       = 0.15;
    public static final double MAX_WEIGHT       = 3.0;
    private static final double SEE_LESS_FACTOR = 0.6;
    private static final double SEE_MORE_FACTOR = 1.5;
    private static final double INTEREST_WEIGHT = 2.0;

    private final CategoryPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    private CategoryPreference getOrCreate(Long userId, Long categoryId) {
        return preferenceRepository.findByUserIdAndCategoryId(userId, categoryId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
                    return new CategoryPreference(user, category, DEFAULT_WEIGHT);
                });
    }

    private double clamp(double w) {
        return Math.max(MIN_WEIGHT, Math.min(MAX_WEIGHT, w));
    }

    @Transactional
    public void seeLess(Long userId, Long categoryId) {
        CategoryPreference pref = getOrCreate(userId, categoryId);
        pref.setWeight(clamp(pref.getWeight() * SEE_LESS_FACTOR));
        preferenceRepository.save(pref);
    }

    @Transactional
    public void seeMore(Long userId, Long categoryId) {
        CategoryPreference pref = getOrCreate(userId, categoryId);
        pref.setWeight(clamp(pref.getWeight() * SEE_MORE_FACTOR));
        preferenceRepository.save(pref);
    }

    /** Explicitly mark as an interest — jumps straight to a strong boost rather than incremental nudging. */
    @Transactional
    public void addInterest(Long userId, Long categoryId) {
        CategoryPreference pref = getOrCreate(userId, categoryId);
        pref.setWeight(Math.max(pref.getWeight(), INTEREST_WEIGHT));
        preferenceRepository.save(pref);
    }

    /** Reset back to neutral — removes the row entirely, since absence == DEFAULT_WEIGHT. */
    @Transactional
    public void resetToDefault(Long userId, Long categoryId) {
        preferenceRepository.findByUserIdAndCategoryId(userId, categoryId)
                .ifPresent(preferenceRepository::delete);
    }

    private String levelLabel(double weight) {
        if (weight <= MIN_WEIGHT * 1.5) return "Seeing much less";
        if (weight < DEFAULT_WEIGHT * 0.9) return "Seeing less";
        if (weight <= DEFAULT_WEIGHT * 1.1) return "Normal";
        if (weight < INTEREST_WEIGHT) return "Seeing more";
        return "Interested";
    }

    @Transactional(readOnly = true)
    public List<CategoryPreferenceResponse> getAllWithPreferences(Long userId) {
        Map<Long, Double> existing = new HashMap<>();
        preferenceRepository.findAllByUserId(userId)
                .forEach(p -> existing.put(p.getCategory().getId(), p.getWeight()));

        return categoryRepository.findAll().stream()
                .filter(c -> !c.isDeleted())
                .map(c -> {
                    double w = existing.getOrDefault(c.getId(), DEFAULT_WEIGHT);
                    return new CategoryPreferenceResponse(
                            c.getId(), c.getName(), c.getSlug(), c.getCoverImage(), w, levelLabel(w));
                })
                .toList();
    }

    /** Lightweight map for the feed sampler — only categories with a non-default row. */
    @Transactional(readOnly = true)
    public Map<Long, Double> getWeightMap(Long userId) {
        Map<Long, Double> map = new HashMap<>();
        preferenceRepository.findAllByUserId(userId)
                .forEach(p -> map.put(p.getCategory().getId(), p.getWeight()));
        return map;
    }
}
