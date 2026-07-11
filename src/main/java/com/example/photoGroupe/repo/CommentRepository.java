package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPinIdAndDeletedFalseOrderByCreatedAtAsc(Long pinId, Pageable pageable);

    long countByPinIdAndDeletedFalse(Long pinId);

    @Query("SELECT c FROM Comment c WHERE c.pin.id = :pinId AND c.parent IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelByPinId(@Param("pinId") Long pinId, Pageable pageable);

    // Replies for a given parent comment
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    Page<Comment> findByPinIdAndParentIsNullAndDeletedFalseOrderByCreatedAtAsc(Long pinId, Pageable pageable);

}
