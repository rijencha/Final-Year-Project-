package com.example.photoGroupe.dto.restrict;

import com.example.photoGroupe.model.restrict.FeedExclusionScope;

import java.time.LocalDateTime;

public class FeedExclusionDtos {

    public static class FeedExclusionRequest {
        private FeedExclusionScope scope;
        private Long pinId;
        private Long excludedUserId;
        private Long categoryId;
        private Long workshopId;

        public FeedExclusionScope getScope()          { return scope; }
        public void setScope(FeedExclusionScope scope) { this.scope = scope; }
        public Long getPinId()                        { return pinId; }
        public void setPinId(Long pinId)               { this.pinId = pinId; }
        public Long getExcludedUserId()                { return excludedUserId; }
        public void setExcludedUserId(Long id)         { this.excludedUserId = id; }
        public Long getCategoryId()                    { return categoryId; }
        public void setCategoryId(Long categoryId)     { this.categoryId = categoryId; }
        public Long getWorkshopId()                    { return workshopId; }
        public void setWorkshopId(Long workshopId)     { this.workshopId = workshopId; }
    }

    public static class FeedExclusionResponse {
        private final Long id;
        private final FeedExclusionScope scope;

        // PIN scope
        private final Long pinId;
        private final String pinTitle;
        private final String pinImageUrl;
        private final Long pinAuthorId;
        private final String pinAuthorName;

        // USER scope
        private final Long excludedUserId;
        private final String excludedUsername;
        private final String excludedFullName;
        private final String excludedProfilePicture;

        // CATEGORY scope
        private final Long categoryId;
        private final String categoryName;
        private final String categoryCoverImage;

        // WORKSHOP scope
        private final Long workshopId;
        private final String workshopTitle;
        private final String workshopCoverImage;
        private final String workshopPhotographerName;

        private final LocalDateTime createdAt;

        public FeedExclusionResponse(Long id, FeedExclusionScope scope,
                                     Long pinId, String pinTitle, String pinImageUrl,
                                     Long pinAuthorId, String pinAuthorName,
                                     Long excludedUserId, String excludedUsername,
                                     String excludedFullName, String excludedProfilePicture,
                                     Long categoryId, String categoryName, String categoryCoverImage,
                                     Long workshopId, String workshopTitle, String workshopCoverImage,
                                     String workshopPhotographerName,
                                     LocalDateTime createdAt) {
            this.id = id;
            this.scope = scope;
            this.pinId = pinId;
            this.pinTitle = pinTitle;
            this.pinImageUrl = pinImageUrl;
            this.pinAuthorId = pinAuthorId;
            this.pinAuthorName = pinAuthorName;
            this.excludedUserId = excludedUserId;
            this.excludedUsername = excludedUsername;
            this.excludedFullName = excludedFullName;
            this.excludedProfilePicture = excludedProfilePicture;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.categoryCoverImage = categoryCoverImage;
            this.workshopId = workshopId;
            this.workshopTitle = workshopTitle;
            this.workshopCoverImage = workshopCoverImage;
            this.workshopPhotographerName = workshopPhotographerName;
            this.createdAt = createdAt;
        }

        public Long getId()                        { return id; }
        public FeedExclusionScope getScope()       { return scope; }

        public Long getPinId()                     { return pinId; }
        public String getPinTitle()                { return pinTitle; }
        public String getPinImageUrl()              { return pinImageUrl; }
        public Long getPinAuthorId()                { return pinAuthorId; }
        public String getPinAuthorName()            { return pinAuthorName; }

        public Long getExcludedUserId()             { return excludedUserId; }
        public String getExcludedUsername()         { return excludedUsername; }
        public String getExcludedFullName()         { return excludedFullName; }
        public String getExcludedProfilePicture()   { return excludedProfilePicture; }

        public Long getCategoryId()                 { return categoryId; }
        public String getCategoryName()             { return categoryName; }
        public String getCategoryCoverImage()       { return categoryCoverImage; }

        public Long getWorkshopId()                 { return workshopId; }
        public String getWorkshopTitle()             { return workshopTitle; }
        public String getWorkshopCoverImage()       { return workshopCoverImage; }
        public String getWorkshopPhotographerName() { return workshopPhotographerName; }

        public LocalDateTime getCreatedAt()         { return createdAt; }
    }
}