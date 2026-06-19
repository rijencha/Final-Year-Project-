package com.example.photoGroupe.service.workshop;

import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkshopService {

    WorkshopDetailResponse createWorkshop(WorkshopRequest req, CustomUserDetails currentUser);

    WorkshopDetailResponse updateWorkshop(Long id, WorkshopRequest req, CustomUserDetails currentUser);

    void deleteWorkshop(Long id, CustomUserDetails currentUser);

    WorkshopDetailResponse updateStatus(Long id, WorkshopStatus status, CustomUserDetails currentUser);

    Page<WorkshopSummaryResponse> listAvailable(Pageable pageable);

    WorkshopDetailResponse getWorkshop(Long id);

    List<WorkshopSummaryResponse> myWorkshops(CustomUserDetails currentUser);

    WorkshopEsewaFormData initiateJoin(Long workshopId, CustomUserDetails currentUser) throws Exception;

    void verifyAndConfirmJoin(String encodedData) throws Exception;


    List<ParticipantResponse> getParticipants(Long workshopId, CustomUserDetails currentUser);
}