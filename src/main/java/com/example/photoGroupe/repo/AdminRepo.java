package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepo extends JpaRepository<User, Long> {

}
