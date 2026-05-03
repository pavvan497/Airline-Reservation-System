package com.pavan.arms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationResponse {
    private String message;
    private String bookingReference;
    private String ticketNumber;
    private String startPlace;
    private String destination;
    private int ticketCount;
    private double totalPrice;
    private String userEmail;
}
