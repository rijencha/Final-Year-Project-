package com.example.photoGroupe.service.admin;

import com.example.photoGroupe.dto.admin.CreateAdminRequest;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.dto.photographer.PhotographerVerificationResponse;
import com.example.photoGroupe.model.VerificationStatus;

import java.util.List;

public interface AdminService {
    UserSummary getUserById(Long id);
    List<UserSummary> getAllUsers(); // Admin: return user+photographer
    void deleteUser(Long id);
    void hardDeleteUser(Long id);       // permanent, SUPER_ADMIN only
    UserSummary restoreUser(Long id);   // undo soft delete
    List<UserSummary> getDeletedUsers();
    UserSummary createAdmin(CreateAdminRequest request);
    List<PhotographerVerificationResponse> getPendingPhotographers();
    PhotographerVerificationResponse updateVerificationStatus(Long id, VerificationStatus newStatus);
}