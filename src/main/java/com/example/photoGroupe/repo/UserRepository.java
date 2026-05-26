package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.OAuthProvider;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRoleIn(List<Role> roles);

    List<User> findByRoleAndVerificationStatusAndDeletedFalse(Role role, VerificationStatus status);

    Optional<User> findByIdAndDeletedFalse(Long id);

    List<User> findByRoleInAndDeletedFalse(List<Role> roles);

    // Deleted users only (for audit/recovery)
    List<User> findByDeletedTrue();

    boolean existsByUsernameAndIdNot(String username, Long id);

    Optional<User> findByOauthProviderAndOauthId(OAuthProvider provider, String oauthId);

}
