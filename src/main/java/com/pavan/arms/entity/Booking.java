package com.pavan.arms.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String bStart;
    private String bEnd;
    private String userEmail;
    private int bNumOfseat;
    private double price;
    @Column(unique = true)
    private String bookingReference;
    private String ticketNumber;
    private LocalDate travelDate;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private LocalDateTime cancelledAt;

}
