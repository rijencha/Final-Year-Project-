package com.example.photoGroupe.service.search;

import com.example.photoGroupe.dto.search.GlobalSearchResponse;

public interface SearchService {
    GlobalSearchResponse globalSearch(String query, Long currentUserId);
}