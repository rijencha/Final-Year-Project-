package com.example.photoGroupe.dto.pins;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponse {
    private Long id;
    private String text;
    private Long authorId;
    private String authorUsername;
    private String authorProfilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted; // ADD
    private boolean edited;
    private int likeCount;
    private boolean likedByCurrentUser;

    // Reply support
    private Long parentId;
    private List<CommentResponse> replies;

    // Getters / Setters
    public Long getId()                             { return id; }
    public String getText()                         { return text; }
    public Long getAuthorId()                       { return authorId; }
    public String getAuthorUsername()               { return authorUsername; }
    public String getAuthorProfilePicture()         { return authorProfilePicture; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public boolean isDeleted()                      { return deleted; }
    public boolean isEdited()                       { return edited; }
    public int getLikeCount()                       { return likeCount; }
    public boolean isLikedByCurrentUser()           { return likedByCurrentUser; }
    public Long getParentId()                       { return parentId; }
    public List<CommentResponse> getReplies()       { return replies; }

    public void setId(Long id)                                          { this.id = id; }
    public void setText(String text)                                    { this.text = text; }
    public void setAuthorId(Long authorId)                              { this.authorId = authorId; }
    public void setAuthorUsername(String authorUsername)                { this.authorUsername = authorUsername; }
    public void setAuthorProfilePicture(String p)                       { this.authorProfilePicture = p; }
    public void setCreatedAt(LocalDateTime createdAt)                   { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)                   { this.updatedAt = updatedAt; }
    public void setDeleted(boolean deleted)                             { this.deleted = deleted; }
    public void setEdited(boolean edited)                               { this.edited = edited; }
    public void setLikeCount(int likeCount)                             { this.likeCount = likeCount; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser)       { this.likedByCurrentUser = likedByCurrentUser; }
    public void setParentId(Long parentId)                              { this.parentId = parentId; }
    public void setReplies(List<CommentResponse> replies)               { this.replies = replies; }
}
