package com.example.photoGroupe.dto.album;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlbumResponse {
    private Long id;
    private String title;
    private String description;
    private String visibility;
    private String coverImageUrl;
    private Long authorId;
    private String authorUsername;
    private String authorProfilePicture;
    private int pinCount;
    private long downloadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long viewCount;
}