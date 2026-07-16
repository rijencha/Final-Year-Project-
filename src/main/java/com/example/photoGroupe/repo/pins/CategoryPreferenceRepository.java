package com.example.photoGroupe.repo.pins;

import com.example.photoGroupe.model.pins.CategoryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryPreferenceRepository extends JpaRepository<CategoryPreference, Long> {
    Optional<CategoryPreference> findByUserIdAndCategoryId(Long userId, Long categoryId);
    List<CategoryPreference> findAllByUserId(Long userId);
}