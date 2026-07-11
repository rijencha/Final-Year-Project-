package com.example.photoGroupe.dto.pins;

import java.time.LocalDateTime;


public class PinResponse {

    private Long id;
    private String imageUrl;
    private String title;
    private String description;
    private String tags;

    private Long   categoryId;
    private String categoryName;
    private String categorySlug;

    // Author snapshot (avoids exposing full User entity)
    private Long authorId;
    private String authorUsername;
    private String authorProfilePicture;
    private String authorRole;
    private String authorName;

    private boolean suspended;
    private String suspensionReason;
    // Counts
    private Integer likeCount;
    private Integer commentCount;
    // Did the current requester like this pin?
    private boolean likedByCurrentUser;

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    private LocalDateTime createdAt;

    // Add to PinResponse.java
    private int     saveCount;
    private int     shareCount;
    private boolean savedByCurrentUser;
    private int downloadCount;

    private long viewCount;


    // ─── Getters / Setters (all fields) ──────────────────────────────────

    public Long getId()                         { return id; }
    public String getImageUrl()                 { return imageUrl; }
    public String getTitle()                    { return title; }
    public String getDescription()              { return description; }
    public String getTags()                     { return tags; }
    public Long getAuthorId()                   { return authorId; }
    public String getAuthorUsername()           { return authorUsername; }
    public String getAuthorProfilePicture()     { return authorProfilePicture; }
    public String getAuthorRole()           { return authorRole; }
    public Integer getLikeCount()                   { return likeCount; }
    public Integer getCommentCount()                { return commentCount; }
    public boolean isLikedByCurrentUser()       { return likedByCurrentUser; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public Long   getCategoryId()                      { return categoryId; }
    public String getCategoryName()                    { return categoryName; }
    public String getCategorySlug()                    { return categorySlug; }
    public boolean isSuspended()               { return suspended; }
    public String getSuspensionReason()        { return suspensionReason; }

    public int getSaveCount() {
        return saveCount;
    }

    public void setSaveCount(int saveCount) {
        this.saveCount = saveCount;
    }

    public int getShareCount() {
        return shareCount;
    }
    public int getDownloadCount() {
        return downloadCount;
    }
    public long getViewCount()               { return viewCount; }

    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }
    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public boolean isSavedByCurrentUser() {
        return savedByCurrentUser;
    }

    public void setSavedByCurrentUser(boolean savedByCurrentUser) {
        this.savedByCurrentUser = savedByCurrentUser;
    }

    public void setSuspended(boolean s)         { this.suspended = s; }
    public void setSuspensionReason(String r)   { this.suspensionReason = r; }
    public void   setCategoryId(Long categoryId)       { this.categoryId = categoryId; }
    public void   setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void   setCategorySlug(String categorySlug) { this.categorySlug = categorySlug; }
    public void setId(Long id)                                      { this.id = id; }
    public void setImageUrl(String imageUrl)                        { this.imageUrl = imageUrl; }
    public void setTitle(String title)                              { this.title = title; }
    public void setDescription(String description)                  { this.description = description; }
    public void setTags(String tags)                                { this.tags = tags; }
    public void setAuthorId(Long authorId)                          { this.authorId = authorId; }
    public void setAuthorUsername(String authorUsername)            { this.authorUsername = authorUsername; }
    public void setAuthorProfilePicture(String authorProfilePicture){ this.authorProfilePicture = authorProfilePicture; }
    public void setAuthorRole(String role)  { this.authorRole = role; }
    public void setLikeCount(Integer likeCount)                         { this.likeCount = likeCount; }
    public void setCommentCount(Integer commentCount)                   { this.commentCount = commentCount; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser)   { this.likedByCurrentUser = likedByCurrentUser; }
    public void setCreatedAt(LocalDateTime createdAt)               { this.createdAt = createdAt; }
}
