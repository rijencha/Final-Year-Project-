package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Friendship;
import com.example.photoGroupe.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("""
        SELECT f FROM Friendship f
        WHERE (f.sender.id = :userId1 AND f.receiver.id = :userId2)
           OR (f.sender.id = :userId2 AND f.receiver.id = :userId1)
    """)
    Optional<Friendship> findBetween(@Param("userId1") Long userId1,
                                     @Param("userId2") Long userId2);

    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND (f.sender.id = :userId OR f.receiver.id = :userId)
    """)
    List<Friendship> findAllAcceptedFriends(@Param("userId") Long userId);

    List<Friendship> findByReceiverIdAndStatus(Long receiverId, FriendshipStatus status);

    List<Friendship> findBySenderIdAndStatus(Long senderId, FriendshipStatus status);

    @Query("""
        SELECT COUNT(f) > 0 FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND ((f.sender.id = :a AND f.receiver.id = :b)
            OR (f.sender.id = :b AND f.receiver.id = :a))
    """)
    boolean areFriends(@Param("a") Long a, @Param("b") Long b);

    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = 'BLOCKED'
          AND (f.sender.id = :userId OR f.receiver.id = :userId)
    """)
    List<Friendship> findBlockedByUser(@Param("userId") Long userId);

    @Query("""
        SELECT f FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND (f.sender.id = :userId OR f.receiver.id = :userId)
          AND (f.sender.id IN (
                SELECT f2.sender.id FROM Friendship f2
                WHERE f2.status = 'ACCEPTED'
                  AND (f2.sender.id = :otherId OR f2.receiver.id = :otherId)
              )
           OR f.receiver.id IN (
                SELECT f2.receiver.id FROM Friendship f2
                WHERE f2.status = 'ACCEPTED'
                  AND (f2.sender.id = :otherId OR f2.receiver.id = :otherId)
              ))
    """)
    List<Friendship> findMutualFriends(@Param("userId") Long userId,
                                       @Param("otherId") Long otherId);

    @Query("""
        SELECT COUNT(f) FROM Friendship f
        WHERE f.status = 'ACCEPTED'
          AND (f.sender.id = :userId OR f.receiver.id = :userId)
    """)
    long countAcceptedFriends(@Param("userId") Long userId);
}