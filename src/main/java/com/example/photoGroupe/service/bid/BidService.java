package com.example.photoGroupe.service.bid;

import com.example.photoGroupe.dto.eventandbid.BidDTO;
import com.example.photoGroupe.dto.eventandbid.BidResponse;
import com.example.photoGroupe.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface BidService {
    BidResponse submitBid(User photographer, Long eventId, BidDTO dto);
    BidResponse editBid(User photographer, Long bidId, BidDTO dto);
    void withdrawBid(User photographer, Long bidId);
    List<BidResponse> getBidsForEvent(Long eventId, Long clientId);
    BidResponse acceptBid(User client, Long bidId);
    BidResponse rejectBid(User client, Long bidId);
    List<BidResponse> getMyBids(Long photographerId);
    BigDecimal getEarnings(Long photographerId);
    void adminRejectBid(Long bidId);
    List<BidResponse> getPublicBidsForEvent(Long eventId);
}
