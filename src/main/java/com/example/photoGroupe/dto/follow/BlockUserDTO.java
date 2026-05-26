package com.example.photoGroupe.dto.follow;

import java.time.LocalDateTime;

public record BlockUserDTO(
        Long userId,
        String username,
        String profilePicture,
        LocalDateTime blockedAt
) {
}
