package com.example.photoGroupe.controller.search;

import com.example.photoGroupe.dto.search.GlobalSearchResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public GlobalSearchResponse search(
            @RequestParam("q") String query,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        Long currentUserId = currentUser != null ? currentUser.getUser().getId() : null;
        return searchService.globalSearch(query, currentUserId);
    }
}
