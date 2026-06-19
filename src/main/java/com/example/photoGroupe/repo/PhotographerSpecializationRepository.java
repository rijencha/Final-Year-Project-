package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.model.event.PhotographerSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PhotographerSpecializationRepository extends JpaRepository<PhotographerSpecialization, Long> {
    List<PhotographerSpecialization> findAllByPhotographerId(Long photographerId);
    boolean existsByPhotographerIdAndCustomType(Long photographerId, String customType);  // ← add
    void deleteByPhotographerIdAndCustomType(Long photographerId, String customType);
    boolean existsByPhotographerIdAndCustomTypeIgnoreCase(Long photographerId, String customType);
    @Query("""
    SELECT DISTINCT s.photographer FROM PhotographerSpecialization s
    JOIN s.category c
    WHERE LOWER(c.name) = LOWER(:name) AND c.deleted = false
""")
    List<User> findPhotographersByCategoryName(@Param("name") String name);

    @Query("""
    SELECT DISTINCT s.photographer FROM PhotographerSpecialization s
    WHERE LOWER(s.customType) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<User> findPhotographersByCustomType(@Param("keyword") String keyword);
    @Query("SELECT DISTINCT s.customType FROM PhotographerSpecialization s WHERE s.customType IS NOT NULL")
    List<String> findAllDistinctCustomTypes();
}