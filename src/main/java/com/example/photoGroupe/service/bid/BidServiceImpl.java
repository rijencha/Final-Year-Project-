package com.example.photoGroupe.service.bid;

import com.example.photoGroupe.dto.eventandbid.BidDTO;
import com.example.photoGroupe.dto.eventandbid.BidResponse;
import com.example.photoGroupe.model.User;
import com.example.photoGroupe.model.bidding.Bid;
import com.example.photoGroupe.model.bidding.BidStatus;
import com.example.photoGroupe.model.event.EventRequest;
import com.example.photoGroupe.model.event.EventRequestStatus;
import com.example.photoGroupe.repo.BidRepository;
import com.example.photoGroupe.repo.EventRequestRepository;
import com.example.photoGroupe.service.booking.BookingService;
import com.example.photoGroupe.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final EventRequestRepository eventRequestRepository;
    private final NotificationService notificationService;
    private final BookingService bookingService;

    @Override
    public BidResponse submitBid(User photographer, Long eventId, BidDTO dto) {
        EventRequest event = eventRequestRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getStatus() != EventRequestStatus.OPEN)
            throw new RuntimeException("This event is no longer accepting bids");

        if (event.getClient().getId().equals(photographer.getId()))
            throw new RuntimeException("You cannot bid on your own event request");

        if (bidRepository.existsByEventRequestIdAndPhotographerId(eventId, photographer.getId()))
            throw new RuntimeException("You have already submitted a bid for this event");

        Bid bid = Bid.builder()
                .eventRequest(event)
                .photographer(photographer)
                .price(dto.getPrice())
                .proposal(dto.getProposal())
                .servicesIncluded(dto.getServicesIncluded())
                .deliveryDays(dto.getDeliveryDays())
                .build();

        Bid saved = bidRepository.save(bid);

        notificationService.create(
                event.getClient(),
                photographer,
                "BID",
                photographer.getFullName() + " placed a bid on your event \"" + event.getTitle() + "\"",
                "/events/" + eventId + "/bids"
        );

        return new BidResponse(saved);
    }

    @Override
    public BidResponse editBid(User photographer, Long bidId, BidDTO dto) {
        Bid bid = findAndValidatePhotographer(bidId, photographer.getId());
        if (bid.getStatus() != BidStatus.PENDING)
            throw new RuntimeException("Only pending bids can be edited");

        bid.setPrice(dto.getPrice());
        bid.setProposal(dto.getProposal());
        bid.setServicesIncluded(dto.getServicesIncluded());
        bid.setDeliveryDays(dto.getDeliveryDays());

        return new BidResponse(bidRepository.save(bid));
    }

    @Override
    public void withdrawBid(User photographer, Long bidId) {
        Bid bid = findAndValidatePhotographer(bidId, photographer.getId());
        if (bid.getStatus() != BidStatus.PENDING)
            throw new RuntimeException("Only pending bids can be withdrawn");

        bid.setStatus(BidStatus.WITHDRAWN);
        bidRepository.save(bid);

        notificationService.create(
                bid.getEventRequest().getClient(),
                photographer,
                "BID_WITHDRAWN",
                photographer.getFullName() + " withdrew their bid",
                "/events/" + bid.getEventRequest().getId() + "/bids"
        );
    }

    @Override
    public List<BidResponse> getBidsForEvent(Long eventId, Long clientId) {
        EventRequest event = eventRequestRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (!event.getClient().getId().equals(clientId))
            throw new RuntimeException("Not authorized");

        return bidRepository.findByEventRequestId(eventId)
                .stream()
                .map(BidResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public BidResponse acceptBid(User client, Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        EventRequest event = bid.getEventRequest();
        if (!event.getClient().getId().equals(client.getId()))
            throw new RuntimeException("Not authorized");
        if (event.getStatus() != EventRequestStatus.OPEN)
            throw new RuntimeException("Event is no longer open");

        bid.setStatus(BidStatus.ACCEPTED);
        bidRepository.save(bid);

        // Reject all other pending bids
        bidRepository.findByEventRequestId(event.getId())
                .stream()
                .filter(b -> !b.getId().equals(bidId) && b.getStatus() == BidStatus.PENDING)
                .forEach(b -> {
                    b.setStatus(BidStatus.REJECTED);
                    bidRepository.save(b);
                    notificationService.create(
                            b.getPhotographer(),
                            client,
                            "BID_REJECTED",
                            "Your bid for \"" + event.getTitle() + "\" was not selected",
                            "/my-bids"
                    );
                });

        event.setStatus(EventRequestStatus.CLOSED);
        eventRequestRepository.save(event);

        bookingService.createFromBid(bid);

        notificationService.create(
                bid.getPhotographer(),
                client,
                "BID_ACCEPTED",
                client.getFullName() + " accepted your bid for \"" + event.getTitle() + "\"",
                "/dashboard"
        );

        return new BidResponse(bid);
    }

    @Override
    public BidResponse rejectBid(User client, Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));

        if (!bid.getEventRequest().getClient().getId().equals(client.getId()))
            throw new RuntimeException("Not authorized");
        if (bid.getStatus() != BidStatus.PENDING)
            throw new RuntimeException("Bid is not pending");

        bid.setStatus(BidStatus.REJECTED);
        bidRepository.save(bid);

        notificationService.create(
                bid.getPhotographer(),
                client,
                "BID_REJECTED",
                "Your bid for \"" + bid.getEventRequest().getTitle() + "\" was rejected",
                "/my-bids"
        );

        return new BidResponse(bid);
    }

    @Override
    public List<BidResponse> getMyBids(Long photographerId) {
        return bidRepository.findByPhotographerId(photographerId)
                .stream()
                .map(BidResponse::new)
                .toList();
    }

    @Override
    public BigDecimal getEarnings(Long photographerId) {
        return bidRepository.sumEarningsByPhotographerId(photographerId);
    }

    @Override
    public void adminRejectBid(Long bidId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        bid.setStatus(BidStatus.REJECTED);
        bidRepository.save(bid);
    }

    @Override
    public List<BidResponse> getPublicBidsForEvent(Long eventId) {
        if (!eventRequestRepository.existsById(eventId))
            throw new RuntimeException("Event not found");

        return bidRepository.findByEventRequestId(eventId)
                .stream()
                .filter(b -> b.getStatus() != BidStatus.WITHDRAWN) // don't surface withdrawn bids
                .map(BidResponse::new)
                .toList();
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private Bid findAndValidatePhotographer(Long bidId, Long photographerId) {
        Bid bid = bidRepository.findById(bidId)
                .orElseThrow(() -> new RuntimeException("Bid not found"));
        if (!bid.getPhotographer().getId().equals(photographerId))
            throw new RuntimeException("Not authorized");
        return bid;
    }
}
