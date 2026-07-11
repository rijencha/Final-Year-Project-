package com.example.photoGroupe.dto.album;

import java.util.List;

public record AlbumZipPayload(String albumTitle, List<AlbumZipItem> items) {
    public record AlbumZipItem(String imageUrl, String title) {}
}