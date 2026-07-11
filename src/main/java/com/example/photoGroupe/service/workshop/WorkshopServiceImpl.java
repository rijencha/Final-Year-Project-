package com.example.photoGroupe.service.workshop;

import com.example.photoGroupe.dto.workshop.WorkshopDTOs.*;
import com.example.photoGroupe.model.Role;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.workshop.Workshop;
import com.example.photoGroupe.model.workshop.WorkshopParticipant;
import com.example.photoGroupe.model.workshop.WorkshopParticipantStatus;
import com.example.photoGroupe.model.workshop.WorkshopStatus;
import com.example.photoGroupe.repo.workshop.WorkshopParticipantRepository;
import com.example.photoGroupe.repo.workshop.WorkshopRepository;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.notification.NotificationService;
import com.example.photoGroupe.service.upload.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopParticipantRepository participantRepository;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService; // add this

    // ─── Photographer: CRUD ───────────────────────────────────────────────

    @Override
    @Transactional
    public WorkshopDetailResponse createWorkshop(WorkshopRequest req, MultipartFile coverImage, CustomUserDetails currentUser) {
        User photographer = currentUser.getUser();
        if (photographer.getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("Only photographers can create workshops");

        Workshop w = new Workshop();
        applyRequest(w, req);
        w.setPhotographer(photographer);
        workshopRepository.save(w); // save first to get ID

        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String url = cloudinaryService.uploadWorkshopCover(coverImage, w.getId());
                w.setCoverImage(url);
                workshopRepository.save(w);
            } catch (IOException e) {
                throw new RuntimeException("Cover image upload failed", e);
            }
        }

        notificationService.create(
                photographer,
                photographer,
                "WORKSHOP_CREATED",
                "Your workshop \"" + w.getTitle() + "\" has been created successfully",
                "/dashboard/workshops/" + w.getId()
        );

        return toDetail(w);
    }

    @Override
    @Transactional
    public WorkshopDetailResponse updateWorkshop(Long id, WorkshopRequest req, MultipartFile coverImage, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        applyRequest(w, req);

        if (coverImage != null && !coverImage.isEmpty()) {
            try {
                String url = cloudinaryService.uploadWorkshopCover(coverImage, w.getId());
                w.setCoverImage(url);
            } catch (IOException e) {
                throw new RuntimeException("Cover image upload failed", e);
            }
        }

        workshopRepository.save(w);

        notificationService.create(
                w.getPhotographer(),
                w.getPhotographer(),
                "WORKSHOP_UPDATED",
                "Your workshop \"" + w.getTitle() + "\" has been updated",
                "/dashboard/workshops/" + w.getId()
        );

        return toDetail(w);
    }

    @Override
    @Transactional
    public void deleteWorkshop(Long id, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        if (w.getSeatsBooked() > 0)
            throw new RuntimeException("Cannot delete a workshop that already has confirmed participants");

        notificationService.create(
                w.getPhotographer(),
                w.getPhotographer(),
                "WORKSHOP_DELETED",
                "Your workshop \"" + w.getTitle() + "\" has been deleted",
                "/dashboard/workshops"
        );
        workshopRepository.delete(w);
    }

    @Override
    @Transactional
    public WorkshopDetailResponse updateStatus(Long id, WorkshopStatus status, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        w.setStatus(status);
        workshopRepository.save(w);
        String message = switch (status) {
            case CANCELLED  -> "Workshop \"" + w.getTitle() + "\" has been cancelled";
            case COMPLETED  -> "Workshop \"" + w.getTitle() + "\" has been marked as completed";
            case ONGOING    -> "Workshop \"" + w.getTitle() + "\" is now ongoing";
            case UPCOMING   -> "Workshop \"" + w.getTitle() + "\" is upcoming";
            case OPEN       -> "Workshop \"" + w.getTitle() + "\" is now open for registration";
        };

        participantRepository.findByWorkshopId(id)
                .stream()
                .filter(wp -> wp.getStatus() == WorkshopParticipantStatus.CONFIRMED)
                .forEach(wp -> notificationService.create(
                        w.getPhotographer(),
                        wp.getParticipant(),
                        "WORKSHOP_STATUS_CHANGED",
                        message,
                        "/workshops/" + w.getId()
                ));
        return toDetail(w);
    }

    // ─── Public: Browse ───────────────────────────────────────────────────

    @Override
    public Page<WorkshopSummaryResponse> listAvailable(Pageable pageable) {
        return workshopRepository.findAvailable(pageable).map(this::toSummary);
    }

    @Override
    public WorkshopDetailResponse getWorkshop(Long id) {
        return toDetail(findById(id));
    }

    @Override
    public List<WorkshopSummaryResponse> myWorkshops(CustomUserDetails currentUser) {
        return workshopRepository
                .findByPhotographerIdOrderByWorkshopDateAsc(currentUser.getUser().getId())
                .stream().map(this::toSummary).toList();
    }
    // ─── Participant Management ────────────────────────────────────────────

    @Override
    public List<ParticipantResponse> getParticipants(Long workshopId, CustomUserDetails currentUser) {
        Workshop w = findById(workshopId);
        if (!w.getPhotographer().getId().equals(currentUser.getUser().getId()))
            throw new RuntimeException("Not authorized to view participants for this workshop");

        return participantRepository.findByWorkshopId(workshopId)
                .stream().map(this::toParticipantResponse).toList();
    }

    @Transactional
    @Override
    public Long registerParticipant(Long workshopId, WorkshopRegistrationRequest req, CustomUserDetails currentUser) {
        Workshop w = findById(workshopId);
        if (w.getStatus() != WorkshopStatus.UPCOMING)
            throw new RuntimeException("Workshop is not open for registration");

        User participant = currentUser.getUser();

        var existing = participantRepository
                .findByWorkshopIdAndParticipantId(workshopId, participant.getId());

        if (existing.isPresent()) {
            WorkshopParticipantStatus existingStatus = existing.get().getStatus();
            if (existingStatus == WorkshopParticipantStatus.PENDING_PAYMENT
                    || existingStatus == WorkshopParticipantStatus.CONFIRMED) {
                throw new RuntimeException("Already registered for this workshop");
            }
            // CANCELLED or REFUNDED -> fall through and let them re-register
        }

        int reserved = workshopRepository.reserveSeat(workshopId);
        if (reserved == 0)
            throw new RuntimeException("Workshop is full");

        WorkshopParticipant wp = existing.orElseGet(WorkshopParticipant::new);
        wp.setWorkshop(w);
        wp.setParticipant(participant);
        wp.setRegistrantName(req.registrantName());
        wp.setRegistrantEmail(req.registrantEmail());
        wp.setRegistrantPhone(req.registrantPhone());
        wp.setStatus(WorkshopParticipantStatus.PENDING_PAYMENT);
        wp.setTransactionUuid(null);
        wp.setPaidAt(null);

        try {
            participantRepository.save(wp);
        } catch (RuntimeException e) {
            workshopRepository.releaseSeat(workshopId);
            throw e;
        }

        return wp.getId();
    }

    @Override
    public ParticipantResponse getMyRegistration(Long workshopId, CustomUserDetails currentUser) {
        return participantRepository
                .findByWorkshopIdAndParticipantId(workshopId, currentUser.getUser().getId())
                .map(this::toParticipantResponse)
                .orElse(null);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────

    private Workshop findById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workshop not found"));
    }

    private Workshop findOwned(Long id, CustomUserDetails currentUser) {
        Workshop w = findById(id);
        if (!w.getPhotographer().getId().equals(currentUser.getUser().getId()))
            throw new RuntimeException("Not authorized to manage this workshop");
        return w;
    }

    private void applyRequest(Workshop w, WorkshopRequest req) {
        w.setTitle(req.title());
        w.setDescription(req.description());
        w.setWorkshopDate(req.workshopDate());
        w.setLocation(req.location());
        w.setDuration(req.duration());
        w.setTotalSeats(req.totalSeats());
        w.setPrice(req.price());
//        w.setCoverImage(req.coverImage());
    }

    // ─── Admin ────────────────────────────────────────────────────────────────

    @Override
    public Page<WorkshopSummaryResponse> listAll(Pageable pageable) {
        return workshopRepository.findAll(pageable).map(this::toSummary);
    }

    @Override
    @Transactional
    public WorkshopDetailResponse adminUpdateStatus(Long id, WorkshopStatus status, User admin) {
        Workshop w = findById(id);
        w.setStatus(status);
        workshopRepository.save(w);

        String message = switch (status) {
            case CANCELLED -> "Your workshop \"" + w.getTitle() + "\" has been cancelled by admin";
            case COMPLETED -> "Your workshop \"" + w.getTitle() + "\" has been marked completed by admin";
            case ONGOING   -> "Your workshop \"" + w.getTitle() + "\" has been marked ongoing by admin";
            case UPCOMING  -> "Your workshop \"" + w.getTitle() + "\" has been marked upcoming by admin";
            case OPEN      -> "Your workshop \"" + w.getTitle() + "\" has been opened by admin";
        };

        // notify photographer
        notificationService.create(
                admin,
                w.getPhotographer(),
                "WORKSHOP_STATUS_CHANGED",
                message,
                "/dashboard/workshops/" + w.getId()
        );

        // notify confirmed participants
        participantRepository.findByWorkshopId(id)
                .stream()
                .filter(wp -> wp.getStatus() == WorkshopParticipantStatus.CONFIRMED)
                .forEach(wp -> notificationService.create(
                        admin,
                        wp.getParticipant(),
                        "WORKSHOP_STATUS_CHANGED",
                        message,
                        "/workshops/" + w.getId()
                ));

        return toDetail(w);
    }

    @Override
    @Transactional
    public void adminDeleteWorkshop(Long id, User admin) {
        Workshop w = findById(id);

        // notify photographer
        notificationService.create(
                admin,
                w.getPhotographer(),
                "WORKSHOP_DELETED",
                "Your workshop \"" + w.getTitle() + "\" has been deleted by admin",
                "/dashboard/workshops"
        );

        // notify confirmed participants
        participantRepository.findByWorkshopId(id)
                .stream()
                .filter(wp -> wp.getStatus() == WorkshopParticipantStatus.CONFIRMED)
                .forEach(wp -> notificationService.create(
                        admin,
                        wp.getParticipant(),
                        "WORKSHOP_DELETED",
                        "Workshop \"" + w.getTitle() + "\" has been cancelled by admin",
                        "/workshops"
                ));

        workshopRepository.delete(w);
    }

    @Override
    public List<ParticipantResponse> adminGetParticipants(Long workshopId) {
        return participantRepository.findByWorkshopId(workshopId)
                .stream().map(this::toParticipantResponse).toList();
    }

    private WorkshopSummaryResponse toSummary(Workshop w) {
        return new WorkshopSummaryResponse(
                w.getId(), w.getTitle(), w.getWorkshopDate(), w.getLocation(),
                w.getDuration(), w.getPrice(), w.getTotalSeats(), w.getSeatsAvailable(),
                w.getCoverImage(), w.getStatus(),
                photographerInfo(w.getPhotographer())
        );
    }

    private WorkshopDetailResponse toDetail(Workshop w) {
        return new WorkshopDetailResponse(
                w.getId(), w.getTitle(), w.getDescription(), w.getWorkshopDate(),
                w.getLocation(), w.getDuration(), w.getPrice(), w.getTotalSeats(),
                w.getSeatsAvailable(), w.getCoverImage(), w.getStatus(),
                photographerInfo(w.getPhotographer()), w.getCreatedAt()
        );
    }

    private PhotographerInfo photographerInfo(User u) {
        return new PhotographerInfo(u.getId(), u.getFullName(), u.getProfilePicture(), u.getActualUsername());
    }

    private ParticipantResponse toParticipantResponse(WorkshopParticipant wp) {
        User u = wp.getParticipant();
        return new ParticipantResponse(
                u.getId(), u.getFullName(), u.getEmail(), u.getActualUsername(),
                u.getProfilePicture(), wp.getStatus(), wp.getRegisteredAt(), wp.getPaidAt()
        );
    }
}