package com.example.photoGroupe.dto.pins;

import org.springframework.web.multipart.MultipartFile;

public class PinRequest {
    private MultipartFile image;
    private String title;
    private String description;
    private String tags;
    private Long   categoryId;
    private String categorySlug;   // add
    private String categoryName;   // add

    public MultipartFile getImage()          { return image; }
    public String getTitle()                 { return title; }
    public String getDescription()           { return description; }
    public String getTags()                  { return tags; }
    public Long   getCategoryId()            { return categoryId; }
    public String getCategorySlug()          { return categorySlug; }
    public String getCategoryName()          { return categoryName; }

    public void setImage(MultipartFile image)          { this.image = image; }
    public void setTitle(String title)                 { this.title = title; }
    public void setDescription(String description)     { this.description = description; }
    public void setTags(String tags)                   { this.tags = tags; }
    public void setCategoryId(Long categoryId)         { this.categoryId = categoryId; }
    public void setCategorySlug(String categorySlug)   { this.categorySlug = categorySlug; }
    public void setCategoryName(String categoryName)   { this.categoryName = categoryName; }
}
