package com.example.photoGroupe.service.workshop;

import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkshopService {

    // change create
    WorkshopDetailResponse createWorkshop(WorkshopRequest req, MultipartFile coverImage, CustomUserDetails currentUser);

    // change update
    WorkshopDetailResponse updateWorkshop(Long id, WorkshopRequest req, MultipartFile coverImage, CustomUserDetails currentUser);

    void deleteWorkshop(Long id, CustomUserDetails currentUser);

    WorkshopDetailResponse updateStatus(Long id, WorkshopStatus status, CustomUserDetails currentUser);

    Page<WorkshopSummaryResponse> listAvailable(Pageable pageable);

    WorkshopDetailResponse getWorkshop(Long id);

    List<WorkshopSummaryResponse> myWorkshops(CustomUserDetails currentUser);

    List<ParticipantResponse> getParticipants(Long workshopId, CustomUserDetails currentUser);

    Long registerParticipant(Long workshopId, WorkshopRegistrationRequest req, CustomUserDetails currentUser);

    ParticipantResponse getMyRegistration(Long workshopId, CustomUserDetails currentUser);

    Page<WorkshopSummaryResponse> searchWorkshops(String query, Pageable pageable);
    // Admin
    Page<WorkshopSummaryResponse> listAll(Pageable pageable);
    WorkshopDetailResponse adminUpdateStatus(Long id, WorkshopStatus status, User admin);
    void adminDeleteWorkshop(Long id, User admin);
    List<ParticipantResponse> adminGetParticipants(Long workshopId);



}