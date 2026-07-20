package com.example.photoGroupe.service.restrict;


import com.example.photoGroupe.model.User;
import com.example.photoGroupe.dto.restrict.RestrictionDtos.*;
import com.example.photoGroupe.model.restrict.RestrictionType;
import com.example.photoGroupe.model.restrict.UserRestriction;
import com.example.photoGroupe.repo.UserRepository;
import com.example.photoGroupe.repo.restrict.UserRestrictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRestrictionService {

    private final UserRestrictionRepository restrictionRepository;
    private final UserRepository userRepository;

    public UserRestrictionService(UserRestrictionRepository restrictionRepository,
                                  UserRepository userRepository) {
        this.restrictionRepository = restrictionRepository;
        this.userRepository = userRepository;
    }

    /**
     * `me` restricts `targetUserId` from performing `type` toward `me`.
     * Idempotent: restricting twice for the same type is a no-op, not an error.
     */
    @Transactional
    public void restrict(Long meId, Long targetUserId, RestrictionType type) {
        if (meId.equals(targetUserId)) {
            throw new IllegalArgumentException("You cannot restrict yourself.");
        }
        if (restrictionRepository.existsByRestrictorIdAndRestrictedIdAndType(meId, targetUserId, type)) {
            return; // already restricted for this action — nothing to do
        }

        User me = userRepository.findById(meId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + meId));
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + targetUserId));

        restrictionRepository.save(new UserRestriction(me, target, type));
    }

    /** Lift one specific restriction. */
    @Transactional
    public void unrestrict(Long meId, Long targetUserId, RestrictionType type) {
        restrictionRepository.deleteByRestrictorIdAndRestrictedIdAndType(meId, targetUserId, type);
    }

    /**
     * The core enforcement check: can `actorId` perform `type` toward `targetId`?
     * Call this from ChatService, CommentService, etc. before the action executes.
     */
    @Transactional(readOnly = true)
    public boolean isRestricted(Long targetId, Long actorId, RestrictionType type) {
        // targetId is the potential restrictor; actorId is the one attempting the action
        return restrictionRepository.existsByRestrictorIdAndRestrictedIdAndType(targetId, actorId, type);
    }

    /** Convenience for controllers/services that want a throwing check. */
    @Transactional(readOnly = true)
    public void assertNotRestricted(Long targetId, Long actorId, RestrictionType type) {
        if (isRestricted(targetId, actorId, type)) {
            // Deliberately generic — see RestrictedActionException javadoc.
            throw new com.example.photoGroupe.exception.RestrictedActionException(
                    "This action is not available right now. You might have been restricted by this Photographer");
        }
    }

    /** All restrictions the current user has placed on others — their settings screen. */
    @Transactional(readOnly = true)
    public List<RestrictionResponse> getMyRestrictions(Long meId) {
        return restrictionRepository.findAllByRestrictorId(meId).stream()
                .map(r -> new RestrictionResponse(
                        r.getId(),
                        r.getRestricted().getId(),
                        r.getRestricted().getActualUsername(),
                        r.getRestricted().getFullName(),
                        r.getRestricted().getProfilePicture(),
                        r.getType(),
                        r.getCreatedAt()))
                .toList();
    }

    /** Which restriction types (if any) does meId currently have on one specific user? */
    @Transactional(readOnly = true)
    public List<RestrictionType> getRestrictionTypesOn(Long meId, Long targetUserId) {
        return restrictionRepository.findAllByRestrictorIdAndRestrictedId(meId, targetUserId).stream()
                .map(UserRestriction::getType)
                .toList();
    }
}