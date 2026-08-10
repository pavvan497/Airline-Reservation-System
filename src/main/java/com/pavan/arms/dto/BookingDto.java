package com.pavan.arms.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    private int id;
    private String bstart;
    private String bend;
//    private String useremail;
    private int bnumofseat;
    private double price;
    private LocalDate travelDate;


}
