package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.ProfileView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    boolean existsByViewerIdAndProfileOwnerIdAndViewedDate(
            Long viewerId, Long profileOwnerId, LocalDate viewedDate);
}