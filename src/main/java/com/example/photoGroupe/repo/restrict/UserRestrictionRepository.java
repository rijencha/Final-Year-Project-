package com.example.photoGroupe.repo.restrict;


import com.example.photoGroupe.model.restrict.RestrictionType;
import com.example.photoGroupe.model.restrict.UserRestriction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRestrictionRepository extends JpaRepository<UserRestriction, Long> {

    // Is `restrictedId` restricted FROM doing `type` TOWARD `restrictorId`?
    boolean existsByRestrictorIdAndRestrictedIdAndType(
            Long restrictorId, Long restrictedId, RestrictionType type);

    Optional<UserRestriction> findByRestrictorIdAndRestrictedIdAndType(
            Long restrictorId, Long restrictedId, RestrictionType type);

    // All restrictions a given user has placed on others (their own settings screen)
    List<UserRestriction> findAllByRestrictorId(Long restrictorId);

    // All restriction types restrictorId has placed on one specific person
    List<UserRestriction> findAllByRestrictorIdAndRestrictedId(Long restrictorId, Long restrictedId);

    void deleteByRestrictorIdAndRestrictedIdAndType(
            Long restrictorId, Long restrictedId, RestrictionType type);
}