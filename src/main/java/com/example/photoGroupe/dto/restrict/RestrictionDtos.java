package com.example.photoGroupe.dto.restrict;

import com.example.photoGroupe.model.restrict.RestrictionType;

import java.time.LocalDateTime;

public class RestrictionDtos {

    /** Body for POST /api/restrictions */
    public static class RestrictionRequest {
        private Long targetUserId;
        private RestrictionType type;

        public Long getTargetUserId()            { return targetUserId; }
        public void setTargetUserId(Long id)      { this.targetUserId = id; }
        public RestrictionType getType()          { return type; }
        public void setType(RestrictionType type) { this.type = type; }
    }

    /** What we return for "my restrictions" screen */
    public static class RestrictionResponse {
        private final Long id;
        private final Long restrictedUserId;
        private final String restrictedUsername;
        private final RestrictionType type;
        private final LocalDateTime createdAt;

        public RestrictionResponse(Long id, Long restrictedUserId, String restrictedUsername,
                                   RestrictionType type, LocalDateTime createdAt) {
            this.id = id;
            this.restrictedUserId = restrictedUserId;
            this.restrictedUsername = restrictedUsername;
            this.type = type;
            this.createdAt = createdAt;
        }

        public Long getId()                    { return id; }
        public Long getRestrictedUserId()      { return restrictedUserId; }
        public String getRestrictedUsername()  { return restrictedUsername; }
        public RestrictionType getType()       { return type; }
        public LocalDateTime getCreatedAt()    { return createdAt; }
    }
}
