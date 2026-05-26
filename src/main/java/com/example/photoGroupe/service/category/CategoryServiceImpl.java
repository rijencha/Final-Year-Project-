package com.example.photoGroupe.service.category;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.photoGroupe.dto.category.CategoryRequest;
import com.example.photoGroupe.dto.category.CategoryResponse;
import com.example.photoGroupe.exception.ResourceAlreadyExistsException;
import com.example.photoGroupe.exception.ResourceNotFoundException;
import com.example.photoGroupe.model.Category;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.CategoryRepository;
import com.example.photoGroupe.service.upload.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final Cloudinary cloudinary;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               CloudinaryService cloudinaryService,
                               Cloudinary cloudinary) {
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.cloudinary = cloudinary;
    }

    @Override
    public CategoryResponse create(CategoryRequest request) {
        String slug = StringUtils.hasText(request.getSlug())
                ? request.getSlug().trim().toLowerCase()
                : toSlug(request.getName());

        if (categoryRepository.existsByNameAndDeletedFalse(request.getName())) {
            throw new ResourceAlreadyExistsException("Category name already in use: " + request.getName());
        }
        if (categoryRepository.existsBySlugAndDeletedFalse(slug)) {
            throw new ResourceAlreadyExistsException("Category slug already in use: " + slug);
        }

        // ── Build entity first ─────────────────────────────────────────
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(slug);
        category.setCreatedBy(currentUser());

        // ── Upload image to Cloudinary ─────────────────────────────────
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                Map<?, ?> result = cloudinaryService.uploadCategoryImage(request.getCoverImage(), slug);
                category.setCoverImage((String) result.get("secure_url"));
                category.setPublicId((String) result.get("public_id"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload category image: " + e.getMessage());
            }
        }
        // ──────────────────────────────────────────────────────────────

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findActiveOrThrow(id);

        String slug = StringUtils.hasText(request.getSlug())
                ? request.getSlug().trim().toLowerCase()
                : toSlug(request.getName());

        if (categoryRepository.existsByNameAndIdNotAndDeletedFalse(request.getName(), id)) {
            throw new ResourceAlreadyExistsException("Category name already in use: " + request.getName());
        }
        if (categoryRepository.existsBySlugAndIdNotAndDeletedFalse(slug, id)) {
            throw new ResourceAlreadyExistsException("Category slug already in use: " + slug);
        }

        // ── Upload new image if provided ───────────────────────────────
        if (request.getCoverImage() != null && !request.getCoverImage().isEmpty()) {
            try {
                Map<?, ?> result = cloudinaryService.uploadCategoryImage(request.getCoverImage(), slug);
                category.setCoverImage((String) result.get("secure_url"));
                category.setPublicId((String) result.get("public_id"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload category image: " + e.getMessage());
            }
        }
        // ──────────────────────────────────────────────────────────────

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(slug);

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return CategoryResponse.from(findActiveOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return CategoryResponse.from(category);
    }

    @Override
    public CategoryResponse getByName(String name) {
        Category category = categoryRepository.findByName(toSlug(name))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return CategoryResponse.from(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAllByDeletedFalse(pageable)
                .map(CategoryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> search(String keyword, Pageable pageable) {
        return categoryRepository.searchByName(keyword, pageable)
                .map(CategoryResponse::from);
    }

    @Override
    public void delete(Long id) {
        Category category = findActiveOrThrow(id);
        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(category);
    }

    @Override
    public void hardDelete(Long id) throws IOException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // ── Delete image from Cloudinary (same pattern as deletePin) ───
        if (StringUtils.hasText(category.getPublicId())) {
            cloudinary.uploader().destroy(category.getPublicId(), ObjectUtils.emptyMap());
        }
        // ──────────────────────────────────────────────────────────────

        categoryRepository.deleteById(id);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private String toSlug(String name) {
        return name.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    private Category findActiveOrThrow(Long id) {
        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }
}