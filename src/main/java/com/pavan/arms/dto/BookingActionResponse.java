package com.pavan.arms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingActionResponse {
    private String message;
    private Integer bookingId;
    private String bookingStatus;
}
