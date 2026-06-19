package com.example.photoGroupe.service.photographer;

import com.example.photoGroupe.dto.eventandbid.EventTypeOptionsResponse;
import com.example.photoGroupe.dto.eventandbid.SpecializationRequest;
import com.example.photoGroupe.dto.eventandbid.SpecializationResponse;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.model.User;

import java.util.List;

public interface PhotographerProfileService {
    List<SpecializationResponse> getSpecializations(Long photographerId);
    SpecializationResponse addSpecialization(User photographer, SpecializationRequest req);
//    void removeSpecialization(User photographer, EventType eventType);
    void removeCustomSpecialization(User photographer, Long id);
    List<PhotographerDetail> getPhotographersByCategory(String keyword);
    EventTypeOptionsResponse getEventTypeOptions();
}
