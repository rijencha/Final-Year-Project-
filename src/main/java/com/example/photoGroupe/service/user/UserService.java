package com.example.photoGroupe.service.user;

import com.example.photoGroupe.dto.auth.ChangePasswordRequest;
import com.example.photoGroupe.dto.detail.UpdateUserRequest;
import com.example.photoGroupe.dto.detail.UpgradeToPhotographerRequest;
import com.example.photoGroupe.dto.photographer.PhotographerDetail;
import com.example.photoGroupe.dto.detail.UserSummary;
import com.example.photoGroupe.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    UserSummary getPublicUserById(Long id);
    List<PhotographerDetail> getAllPhotographers();
    PhotographerDetail getPhotographerDetail(Long id);
    UserSummary getUserById(Long id);
    List<UserSummary> getAllUsers();
    String updateProfilePicture(Long userId, MultipartFile file) throws IOException;
    UserSummary updateUser(Long userId, UpdateUserRequest request, User currentUser);
    UserSummary upgradeToPhotographer(Long userId, UpgradeToPhotographerRequest request);
    void updateInterests(Long userId, List<String> interests, Long currentUserId);
    List<PhotographerDetail> getTopPhotographers(int limit);
    void recordProfileView(Long profileOwnerId, Long viewerId);
    void changePassword(Long userId, ChangePasswordRequest request, User currentUser);
}
