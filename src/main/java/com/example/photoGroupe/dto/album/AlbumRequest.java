package com.example.photoGroupe.dto.album;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AlbumRequest {
    private String title;
    private String description;
    private String visibility = "PUBLIC"; // PUBLIC | PRIVATE
    private MultipartFile coverImage;
}