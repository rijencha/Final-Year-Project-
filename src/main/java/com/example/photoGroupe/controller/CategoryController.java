package com.example.photoGroupe.controller;

import com.example.photoGroupe.dto.category.CategoryRequest;
import com.example.photoGroupe.dto.category.CategoryResponse;
import com.example.photoGroupe.service.category.CategoryService;
import com.example.photoGroupe.service.upload.PinsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/users/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAll(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] parts   = sort.split(",");
        String   field   = parts[0];
        Sort.Direction dir = parts.length > 1 && parts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, field));
        return ResponseEntity.ok(categoryService.getAll(pageable));
    }

    /**
     * GET /api/v1/categories/search?keyword=landscape&page=0&size=10
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(categoryService.search(keyword, pageable));
    }

    /**
     * GET /api/v1/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    /**
     * GET /api/v1/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getBySlug(slug));
    }

    @GetMapping("/name")
    public ResponseEntity<CategoryResponse> getByName(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getBySlug(name));
    }

    // ─── Admin Endpoints ──────────────────────────────────────────────────

    /**
     * POST /api/v1/categories
     * Only ADMIN and SUPER_ADMIN may create categories.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> create(@ModelAttribute CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @ModelAttribute CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    /**
     * DELETE /api/v1/categories/{id}  → soft delete
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/v1/categories/{id}/hard  → permanent delete (SUPER_ADMIN only)
     */
    @DeleteMapping("/{id}/hard")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> hardDelete(@PathVariable Long id) throws IOException {
        categoryService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }
}
