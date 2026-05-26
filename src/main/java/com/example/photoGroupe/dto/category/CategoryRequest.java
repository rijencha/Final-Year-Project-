package com.example.photoGroupe.dto.category;

import org.springframework.web.multipart.MultipartFile;

public class CategoryRequest {
    private String name;
    private String description;
    /**
     * Optional: if blank the service auto-generates a slug from the name.
     */
    private String slug;

    private MultipartFile coverImage;

    public CategoryRequest() {}

    public CategoryRequest(String name, String description, String slug, String coverImage) {
        this.name = name;
        this.description = description;
        this.slug = slug;
    }

    public String getName()          { return name; }
    public String getDescription()   { return description; }
    public String getSlug()          { return slug; }
    public MultipartFile getCoverImage()    { return coverImage; }

    public void setName(String name)                { this.name = name; }
    public void setDescription(String description)  { this.description = description; }
    public void setSlug(String slug)                { this.slug = slug; }
    public void setCoverImage(MultipartFile coverImage)    { this.coverImage = coverImage; }
}
