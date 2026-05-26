package com.example.photoGroupe.dto.friends;

import com.example.photoGroupe.model.FriendshipStatus;

import java.time.LocalDateTime;

// FriendshipResponseDto.java
public record FriendshipResponseDto(
        Long friendshipId,
        Long friendId,
        String friendUsername,
        String friendFullName,
        String profilePicture,
        FriendshipStatus status,
        boolean isSender,
        LocalDateTime since
) {}


