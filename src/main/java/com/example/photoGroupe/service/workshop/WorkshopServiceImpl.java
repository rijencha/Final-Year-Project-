package com.example.photoGroupe.service.workshop;

import com.example.photoGroupe.config.EsewaConfig;
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
import com.example.photoGroupe.util.EsewaSignatureUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkshopServiceImpl implements WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopParticipantRepository participantRepository;
    private final EsewaConfig esewaConfig;
    private final EsewaSignatureUtil signatureUtil;
    private final NotificationService notificationService;

    // ─── Photographer: CRUD ───────────────────────────────────────────────

    @Override
    @Transactional
    public WorkshopDetailResponse createWorkshop(WorkshopRequest req, CustomUserDetails currentUser) {
        User photographer = currentUser.getUser();
        if (photographer.getRole() != Role.PHOTOGRAPHER)
            throw new RuntimeException("Only photographers can create workshops");

        Workshop w = new Workshop();
        applyRequest(w, req);
        w.setPhotographer(photographer);
        workshopRepository.save(w);
        return toDetail(w);
    }

    @Override
    @Transactional
    public WorkshopDetailResponse updateWorkshop(Long id, WorkshopRequest req, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        applyRequest(w, req);
        workshopRepository.save(w);
        return toDetail(w);
    }

    @Override
    @Transactional
    public void deleteWorkshop(Long id, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        if (w.getSeatsBooked() > 0)
            throw new RuntimeException("Cannot delete a workshop that already has confirmed participants");
        workshopRepository.delete(w);
    }

    @Override
    @Transactional
    public WorkshopDetailResponse updateStatus(Long id, WorkshopStatus status, CustomUserDetails currentUser) {
        Workshop w = findOwned(id, currentUser);
        w.setStatus(status);
        workshopRepository.save(w);
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

    // ─── Participant: eSewa Payment Flow ──────────────────────────────────

    @Override
    @Transactional
    public WorkshopEsewaFormData initiateJoin(Long workshopId, CustomUserDetails currentUser) throws Exception {
        Workshop w = findById(workshopId);
        User participant = currentUser.getUser();

        if (w.getStatus() != WorkshopStatus.UPCOMING)
            throw new RuntimeException("This workshop is not open for registration");

        if (w.isFull())
            throw new RuntimeException("No seats available for this workshop");

        if (participantRepository.existsByWorkshopIdAndParticipantId(workshopId, participant.getId())) {
            WorkshopParticipant existing = participantRepository
                    .findByWorkshopIdAndParticipantId(workshopId, participant.getId()).get();
            if (existing.getStatus() == WorkshopParticipantStatus.CONFIRMED)
                throw new RuntimeException("You have already joined this workshop");
            if (existing.getStatus() == WorkshopParticipantStatus.PENDING_PAYMENT)
                throw new RuntimeException("Payment already initiated — please complete eSewa checkout");
        }

        String transactionUuid = UUID.randomUUID().toString();
        String totalAmount = w.getPrice()
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString();

        String signature = signatureUtil.generateSignature(
                totalAmount, transactionUuid,
                esewaConfig.getMerchantCode(), esewaConfig.getSecretKey()
        );

        WorkshopParticipant wp = new WorkshopParticipant();
        wp.setWorkshop(w);
        wp.setParticipant(participant);
        wp.setTransactionUuid(transactionUuid);
        wp.setStatus(WorkshopParticipantStatus.PENDING_PAYMENT);
        participantRepository.save(wp);

        return new WorkshopEsewaFormData(
                totalAmount,                                    // totalAmount
                "0",                                           // taxAmount
                "0",                                           // productServiceCharge
                transactionUuid,                               // transactionUuid
                esewaConfig.getMerchantCode(),                 // productCode
                "0",                                           // productDeliveryCharge
                esewaConfig.getSuccessUrl(),                   // successUrl
                esewaConfig.getFailureUrl(),                   // failureUrl
                "total_amount,transaction_uuid,product_code",  // signedFieldNames
                signature,                                     // signature
                esewaConfig.getPaymentUrl()                    // paymentUrl
        );
    }

    @Override
    @Transactional
    public void verifyAndConfirmJoin(String encodedData) throws Exception {
        String decoded = new String(Base64.getDecoder().decode(encodedData));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> responseData = mapper.readValue(decoded, Map.class);

        String signedFieldNames = responseData.get("signed_field_names");
        String receivedSig      = responseData.get("signature");

        // Build dynamic message from signed_field_names
        String[] fields = signedFieldNames.split(",");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].trim();
            sb.append(field).append("=").append(responseData.get(field));
            if (i < fields.length - 1) sb.append(",");
        }
        String message = sb.toString();

        // Verify HMAC-SHA256 signature
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                esewaConfig.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String expectedSig = Base64.getEncoder().encodeToString(
                mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));

        if (!expectedSig.equals(receivedSig))
            throw new RuntimeException("Invalid eSewa signature");

        String transactionUuid = responseData.get("transaction_uuid");
        String totalAmount     = responseData.get("total_amount");
        String productCode     = responseData.get("product_code");

        // Cross-check with eSewa status API
        String verifyUrl = esewaConfig.getVerifyUrl()
                + "?product_code=" + productCode
                + "&transaction_uuid=" + transactionUuid
                + "&total_amount=" + totalAmount;

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> verifyResponse = restTemplate.getForEntity(verifyUrl, Map.class);
        Map<String, Object> body = verifyResponse.getBody();

        if (!"COMPLETE".equals(body.get("status")))
            throw new RuntimeException("Payment not completed on eSewa side");

        // Confirm participant & lock seat
        WorkshopParticipant wp = participantRepository.findByTransactionUuid(transactionUuid)
                .orElseThrow(() -> new RuntimeException("Participant record not found"));

        Workshop w = wp.getWorkshop();

        if (w.isFull())
            throw new RuntimeException("Workshop is now full — contact support for a refund");

        wp.setStatus(WorkshopParticipantStatus.CONFIRMED);
        wp.setPaidAt(LocalDateTime.now());
        participantRepository.save(wp);

        w.setSeatsBooked(w.getSeatsBooked() + 1);
        workshopRepository.save(w);

        // Notify photographer
        notificationService.create(
                w.getPhotographer(),
                wp.getParticipant(),
                "WORKSHOP_JOINED",
                wp.getParticipant().getFullName() + " joined your workshop \""
                        + w.getTitle() + "\" — "
                        + w.getSeatsAvailable() + " seat(s) remaining",
                "/dashboard/workshops/" + w.getId()
        );
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
        w.setCoverImage(req.coverImage());
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