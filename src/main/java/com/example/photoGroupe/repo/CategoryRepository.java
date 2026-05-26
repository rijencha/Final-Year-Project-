package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByIdAndDeletedFalse(Long id);

    /** Active category by slug */
    Optional<Category> findBySlugAndDeletedFalse(String slug);

    /** Active category by name */
    Optional<Category> findByNameAndDeletedFalse(String name);

    /** Check slug uniqueness (excluding a given id – useful for updates) */
    boolean existsBySlugAndIdNotAndDeletedFalse(String slug, Long id);

    /** Check name uniqueness (excluding a given id) */
    boolean existsByNameAndIdNotAndDeletedFalse(String name, Long id);

    boolean existsBySlugAndDeletedFalse(String slug);
    boolean existsByNameAndDeletedFalse(String name);

    /** All active categories, paginated */
    Page<Category> findAllByDeletedFalse(Pageable pageable);

    /** Search by name (case-insensitive) */
    @Query("SELECT c FROM Category c WHERE c.deleted = false AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Category> searchByName(String keyword, Pageable pageable);

    Optional<Category> findBySlug(String slug);

    Optional<Category> findByName(String name);
}
