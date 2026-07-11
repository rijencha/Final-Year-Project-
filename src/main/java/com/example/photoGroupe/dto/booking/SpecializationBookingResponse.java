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
public class SpecializationBookingResponse {

    private String specializationName;   // category name, or customType if no category
    private long bookingCount;
    private BigDecimal totalRevenue;
    private List<BookingResponse> bookings;
}