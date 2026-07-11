package com.example.photoGroupe.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventTypeBookingResponse {
    private String eventTypeName;   // e.g. "WEDDING", or customEventType if eventType is null
    private long bookingCount;
    private BigDecimal totalRevenue;
    private List<BookingResponse> bookings;
}