package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.OAuthProvider;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.VerificationStatus;
import org.springframework.data.domain.Pageable;
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

    @Query("""
    select u from User u
    where u.deleted = false and u.enabled = true
      and (
        lower(u.username) like lower(concat('%', :q, '%'))
        or lower(u.fullName) like lower(concat('%', :q, '%'))
      )
""")
    List<User> searchByNameOrUsername(@Param("q") String q, Pageable pageable);

    @Query("""
    select u from User u
    where u.deleted = false and u.enabled = true
      and u.role = 'PHOTOGRAPHER' and u.verificationStatus = 'APPROVED'
      and (
        lower(u.username) like lower(concat('%', :q, '%'))
        or lower(u.fullName) like lower(concat('%', :q, '%'))
        or lower(u.location) like lower(concat('%', :q, '%'))
        or lower(u.bio) like lower(concat('%', :q, '%'))
      )
""")
    List<User> searchPhotographers(@Param("q") String q, Pageable pageable);

}
