package com.example.photoGroupe.service.friends;

import com.example.photoGroupe.dto.follow.BlockUserDTO;
import com.example.photoGroupe.model.Block;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.repo.BlockRepository;
import com.example.photoGroupe.repo.FollowRepository;
import com.example.photoGroupe.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BlockServiceImpl implements BlockService {

    private final BlockRepository blockRepo;
    private final FollowRepository followRepo;
    private final UserRepository userRepo;

    public BlockServiceImpl(BlockRepository blockRepo,
                            FollowRepository followRepo,
                            UserRepository userRepo) {
        this.blockRepo  = blockRepo;
        this.followRepo = followRepo;
        this.userRepo   = userRepo;
    }

    // ─── block ────────────────────────────────────────────────────────────

    @Override
    public void block(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot block yourself.");
        }

        User blocker = loadUser(currentUserId);
        User blocked = loadUser(targetUserId);

        // Idempotent — ignore if already blocked
        if (blockRepo.existsByBlockerAndBlocked(blocker, blocked)) return;

        // Persist the block
        blockRepo.save(new Block(blocker, blocked));

        // Tear down follow relationships in BOTH directions silently
        followRepo.findByFollowerAndFollowing(blocker, blocked)
                .ifPresent(followRepo::delete);
        followRepo.findByFollowerAndFollowing(blocked, blocker)
                .ifPresent(followRepo::delete);
    }

    // ─── unblock ──────────────────────────────────────────────────────────

    @Override
    public void unblock(Long currentUserId, Long targetUserId) {
        User blocker = loadUser(currentUserId);
        User blocked = loadUser(targetUserId);

        Block block = blockRepo.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No active block found."));

        blockRepo.delete(block);
        // Follow is NOT restored — user decides whether to re-follow
    }

    // ─── removeFollower ───────────────────────────────────────────────────

    @Override
    public void removeFollower(Long currentUserId, Long followerUserId) {
        // "Remove follower" = delete the row where follower=followerUserId, following=currentUserId
        User me       = loadUser(currentUserId);
        User follower = loadUser(followerUserId);

        followRepo.findByFollowerAndFollowing(follower, me)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "That user is not following you."));

        followRepo.deleteByFollowerAndFollowing(follower, me);
    }

        // ─── getBlockedUsers ──────────────────────────────────────────────────

    @Override
    public Page<BlockUserDTO> getBlockedUsers(Long currentUserId, Pageable pageable) {
        User blocker = loadUser(currentUserId);
        return blockRepo.findByBlocker(blocker, pageable)
                .map(this::toDTO);
    }

    // ─── isBlocking ───────────────────────────────────────────────────────

    @Override
    public boolean isBlocking(Long currentUserId, Long targetUserId) {
        return blockRepo.existsByBlockerAndBlocked(
                loadUser(currentUserId), loadUser(targetUserId));
    }

    // ─── helpers ──────────────────────────────────────────────────────────

    private User loadUser(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "User not found: " + id));
    }

    private BlockUserDTO toDTO(Block b) {
        User u = b.getBlocked();
        return new BlockUserDTO(
                u.getId(),
                u.getUsername(),
                u.getProfilePicture(),   // adjust to your actual User field
                b.getCreatedAt()
        );
    }
}
