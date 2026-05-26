package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Block;
import com.example.photoGroupe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Long> {
    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    /** True if EITHER party has blocked the other — use before allowing follows/DMs */
    @Query("""
        SELECT COUNT(b) > 0 FROM Block b
        WHERE (b.blocker.id = :userAId AND b.blocked.id = :userBId)
           OR (b.blocker.id = :userBId AND b.blocked.id = :userAId)
        """)
    boolean existsBlockBetween(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId
    );

    /** All users that `blocker` has blocked, paginated */
    Page<Block> findByBlocker(User blocker, Pageable pageable);

    /** Count how many users a given user has blocked */
    long countByBlocker(User blocker);

    void deleteByBlockerAndBlocked(User blocker, User blocked);
}
