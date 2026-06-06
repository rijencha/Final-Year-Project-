package com.example.photoGroupe.controller.booking;

import com.example.photoGroupe.dto.eventandbid.BidDTO;
import com.example.photoGroupe.dto.eventandbid.BidResponse;
import com.example.photoGroupe.security.CustomUserDetails;
import com.example.photoGroupe.service.bid.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/bids")
@RequiredArgsConstructor
public class BidController {
    private final BidService bidService;

    // Photographer — submit bid
    @PostMapping("/event/{eventId}")
    public ResponseEntity<BidResponse> submit(
            @PathVariable Long eventId,
            @RequestBody BidDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.submitBid(currentUser.getUser(), eventId, dto));
    }

    // Photographer — edit bid
    @PutMapping("/{bidId}")
    public ResponseEntity<BidResponse> edit(
            @PathVariable Long bidId,
            @RequestBody BidDTO dto,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.editBid(currentUser.getUser(), bidId, dto));
    }

    // Photographer — withdraw bid
    @PutMapping("/{bidId}/withdraw")
    public ResponseEntity<Void> withdraw(
            @PathVariable Long bidId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        bidService.withdrawBid(currentUser.getUser(), bidId);
        return ResponseEntity.ok().build();
    }

    // Photographer — my bids
    @GetMapping("/my")
    public ResponseEntity<List<BidResponse>> myBids(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.getMyBids(currentUser.getId()));
    }

    // Photographer — earnings
    @GetMapping("/my/earnings")
    public ResponseEntity<Map<String, BigDecimal>> myEarnings(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(Map.of("total", bidService.getEarnings(currentUser.getId())));
    }

    // Client — view bids on their event
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BidResponse>> getBidsForEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.getBidsForEvent(eventId, currentUser.getId()));
    }

    // Client — accept bid
    @PutMapping("/{bidId}/accept")
    public ResponseEntity<BidResponse> accept(
            @PathVariable Long bidId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.acceptBid(currentUser.getUser(), bidId));
    }

    // Client — reject bid
    @PutMapping("/{bidId}/reject")
    public ResponseEntity<BidResponse> reject(
            @PathVariable Long bidId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bidService.rejectBid(currentUser.getUser(), bidId));
    }

    // Admin — force reject
    @PutMapping("/admin/{bidId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> adminReject(@PathVariable Long bidId) {
        bidService.adminRejectBid(bidId);
        return ResponseEntity.ok().build();
    }
}
