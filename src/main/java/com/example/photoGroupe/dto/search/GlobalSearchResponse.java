package com.example.photoGroupe.dto.search;

import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.pins.PinResponse;
import com.example.photoGroupe.dto.workshop.WorkshopDTOs.WorkshopSummaryResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GlobalSearchResponse {
    private String query;
    private boolean exactMatchFound;

    private List<UserSummary> users;
    private List<PhotographerDetail> photographers;
    private List<WorkshopSummaryResponse> workshops;
    private List<PinResponse> pins;

    /** Only populated when nothing matched, so the page isn't empty. */
    private List<PinResponse> suggestedPins;
}