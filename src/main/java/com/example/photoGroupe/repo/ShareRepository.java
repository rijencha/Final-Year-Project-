package com.example.photoGroupe.repo;

import com.example.photoGroupe.model.share.Share;
import com.example.photoGroupe.model.share.ShareableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShareRepository extends JpaRepository<Share, Long> {

    @Query("select count(distinct s.sharedBy.id) from Share s where s.entityType = :type and s.entityId = :entityId")
    long countDistinctSharers(@Param("type") ShareableType type, @Param("entityId") Long entityId);
}