package com.example.photoGroupe.repo.workshop;

import com.example.photoGroupe.model.workshop.WorkshopParticipant;
import com.example.photoGroupe.model.workshop.WorkshopParticipantStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkshopParticipantRepository extends JpaRepository<WorkshopParticipant, Long> {

    boolean existsByWorkshopIdAndParticipantId(Long workshopId, Long participantId);

    Optional<WorkshopParticipant> findByWorkshopIdAndParticipantId(Long workshopId, Long participantId);

    Optional<WorkshopParticipant> findByTransactionUuid(String transactionUuid);

    List<WorkshopParticipant> findByParticipantIdAndStatus(Long participantId, WorkshopParticipantStatus status);

    List<WorkshopParticipant> findByWorkshopId(Long workshopId);
}