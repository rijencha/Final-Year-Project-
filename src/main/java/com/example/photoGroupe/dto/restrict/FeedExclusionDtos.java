package com.example.photoGroupe.dto.restrict;

import com.example.photoGroupe.model.restrict.FeedExclusionScope;

import java.time.LocalDateTime;

public class FeedExclusionDtos {
    public static class FeedExclusionRequest {
        private FeedExclusionScope scope;
        private Long pinId;
        private Long excludedUserId;
        private Long categoryId;

        public FeedExclusionScope getScope()          { return scope; }
        public void setScope(FeedExclusionScope scope) { this.scope = scope; }
        public Long getPinId()                        { return pinId; }
        public void setPinId(Long pinId)               { this.pinId = pinId; }
        public Long getExcludedUserId()                { return excludedUserId; }
        public void setExcludedUserId(Long id)         { this.excludedUserId = id; }
        public Long getCategoryId()                    { return categoryId; }
        public void setCategoryId(Long categoryId)     { this.categoryId = categoryId; }
    }

    public static class FeedExclusionResponse {
        private final Long id;
        private final FeedExclusionScope scope;
        private final Long pinId;
        private final Long excludedUserId;
        private final String excludedUsername;
        private final Long categoryId;
        private final String categoryName;
        private final LocalDateTime createdAt;

        public FeedExclusionResponse(Long id, FeedExclusionScope scope, Long pinId,
                                     Long excludedUserId, String excludedUsername,
                                     Long categoryId, String categoryName, LocalDateTime createdAt) {
            this.id = id;
            this.scope = scope;
            this.pinId = pinId;
            this.excludedUserId = excludedUserId;
            this.excludedUsername = excludedUsername;
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.createdAt = createdAt;
        }

        public Long getId()                  { return id; }
        public FeedExclusionScope getScope() { return scope; }
        public Long getPinId()               { return pinId; }
        public Long getExcludedUserId()      { return excludedUserId; }
        public String getExcludedUsername()  { return excludedUsername; }
        public Long getCategoryId()          { return categoryId; }
        public String getCategoryName()      { return categoryName; }
        public LocalDateTime getCreatedAt()  { return createdAt; }
    }
}