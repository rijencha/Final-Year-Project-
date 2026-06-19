package com.example.photoGroupe.dto.eventandbid;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventTypeOptionsResponse {
    private List<String> standard;   // from EventType enum
    private List<String> custom;     // from photographer specializations
    private List<String> all;        // merged, deduplicated, sorted
}