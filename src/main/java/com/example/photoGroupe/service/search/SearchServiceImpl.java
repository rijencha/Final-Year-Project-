package com.example.photoGroupe.service.search;

import com.example.photoGroupe.dto.search.GlobalSearchResponse;
import com.example.photoGroupe.service.upload.PinsService;
import com.example.photoGroupe.service.user.UserService;
import com.example.photoGroupe.service.workshop.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int USERS_LIMIT = 10;
    private static final int PHOTOGRAPHERS_LIMIT = 10;
    private static final int WORKSHOPS_LIMIT = 10;
    private static final int PINS_LIMIT = 20;
    private static final int FALLBACK_LIMIT = 12;

    private final UserService userService;
    private final WorkshopService workshopService;
    private final PinsService pinsService;

    @Override
    public GlobalSearchResponse globalSearch(String query, Long currentUserId) {
        String q = query == null ? "" : query.trim();

        if (q.isBlank()) {
            return emptyQueryResponse(currentUserId);
        }

        List<?> users = userService.searchUsers(q, USERS_LIMIT);
        var photographers = userService.searchPhotographers(q, PHOTOGRAPHERS_LIMIT);
        var workshops = workshopService.searchWorkshops(q, PageRequest.of(0, WORKSHOPS_LIMIT)).getContent();
        var pins = pinsService.searchPins(q, 0, PINS_LIMIT, currentUserId).getContent();

        boolean found = !users.isEmpty() || !photographers.isEmpty()
                || !workshops.isEmpty() || !pins.isEmpty();

        var builder = GlobalSearchResponse.builder()
                .query(q)
                .exactMatchFound(found)
                .users((List) users)
                .photographers(photographers)
                .workshops(workshops)
                .pins(pins);

        // Nothing matched anywhere -> fall back to trending pins so the
        // search results page never renders completely blank.
        builder.suggestedPins(found ? List.of() : pinsService.getTopPins(FALLBACK_LIMIT, currentUserId));

        return builder.build();
    }

    private GlobalSearchResponse emptyQueryResponse(Long currentUserId) {
        return GlobalSearchResponse.builder()
                .query("")
                .exactMatchFound(false)
                .users(List.of())
                .photographers(List.of())
                .workshops(List.of())
                .pins(List.of())
                .suggestedPins(pinsService.getTopPins(FALLBACK_LIMIT, currentUserId))
                .build();
    }
}