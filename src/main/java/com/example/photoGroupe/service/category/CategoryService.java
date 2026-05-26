package com.example.photoGroupe.service.category;

import com.example.photoGroupe.dto.category.CategoryRequest;
import com.example.photoGroupe.dto.category.CategoryResponse;
import com.example.photoGroupe.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface CategoryService {
    CategoryResponse create(CategoryRequest request);

    /** Fetch a single active category by id. */
    CategoryResponse getById(Long id);

    /** Fetch a single active category by its URL slug. */
    CategoryResponse getBySlug(String slug);

    CategoryResponse getByName(String name);

    /** Paginated list of all active categories. */
    Page<CategoryResponse> getAll(Pageable pageable);

    /** Paginated keyword search on category names. */
    Page<CategoryResponse> search(String keyword, Pageable pageable);

    /** Update a category's mutable fields. */
    CategoryResponse update(Long id, CategoryRequest request);

    /** Soft-delete a category (sets deleted = true, deletedAt = now). */
    void delete(Long id);

    /** Hard-delete – use only for admin cleanup or tests. */
    void hardDelete(Long id) throws IOException;
}
