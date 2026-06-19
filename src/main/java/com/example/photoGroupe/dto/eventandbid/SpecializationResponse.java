package com.example.photoGroupe.dto.eventandbid;

import com.example.photoGroupe.model.event.EventType;
import com.example.photoGroupe.model.event.PhotographerSpecialization;
import lombok.Data;

@Data
public class SpecializationResponse {
    private Long id;
    private String customType;
    private String note;
    private Long categoryId;
    private String categorySlug;   // ← add
    private String categoryName;

    public static SpecializationResponse from(PhotographerSpecialization s) {
        SpecializationResponse r = new SpecializationResponse();
        r.id = s.getId();
        r.customType = s.getCustomType();
        r.note = s.getNote();
        if (s.getCategory() != null) {
            r.categoryId   = s.getCategory().getId();
            r.categorySlug = s.getCategory().getSlug();
            r.categoryName = s.getCategory().getName();
        }
        return r;
    }
}
