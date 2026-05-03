package com.jkshian.arms.service;


import com.jkshian.arms.dto.BookingDto;
import com.jkshian.arms.dto.BookingConfirmationResponse;
import com.jkshian.arms.entity.AirPlane;
import com.jkshian.arms.entity.Booking;
import com.jkshian.arms.repo.BookingRepo;
import com.jkshian.arms.repo.PlaneRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final double BASE_FARE_PER_KM = 12.0;
    private static final String BOOKING_REFERENCE_PREFIX = "ARMS";
    private static final String TICKET_NUMBER_PREFIX = "TKT";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final PlaneRepo planeRepo;
    private final BookingRepo bookingRepo;


    public List<Booking> getAllBooking() {
        return bookingRepo.findAll();
    }


    public ResponseEntity<Double> checkPrice(BookingDto bookingdto) {
        if (hasSameRouteEndpoints(bookingdto)) {
            return ResponseEntity.badRequest().build();
        }
        AirPlane findPlane =planeIsAvilable(bookingdto);
        if(findPlane!=null){
              bookingdto.setPrice(calculatePrice(findPlane.getNumOfKm(),bookingdto.getBnumofseat()));
            return ResponseEntity.ok(bookingdto.getPrice());
          }else {
           return ResponseEntity.notFound().build();
        }
    }

    private AirPlane planeIsAvilable(BookingDto bookingdto){
        AirPlane findPlane = planeRepo.findByStartAndEndIgnoreCase(bookingdto.getBstart(),bookingdto.getBend());
        if(findPlane == null){
            ResponseEntity.status(401).body("Your enteres plane is not avilable");
            return null;
        }else {
            return findPlane;
        }
    }

    private double calculatePrice(double numOfKm, int bnumOfseat) {
        return numOfKm * bnumOfseat * BASE_FARE_PER_KM;
    }

    public ResponseEntity<BookingConfirmationResponse> addBooking(BookingDto bookingdto) {
        if (hasSameRouteEndpoints(bookingdto)) {
            return ResponseEntity.badRequest().body(
                    BookingConfirmationResponse.builder()
                            .message("Source and destination cannot be the same.")
                            .build()
            );
        }
        AirPlane findPlane =planeIsAvilable(bookingdto);
        Booking booking = new Booking();
        if(findPlane != null){

            booking.setPrice(calculatePrice(findPlane.getNumOfKm(),bookingdto.getBnumofseat()));
            booking.setBStart(bookingdto.getBstart());
            booking.setBEnd(bookingdto.getBend());
            booking.setBNumOfseat(bookingdto.getBnumofseat());
            booking.setUserEmail(resolveCurrentUserEmail());
            booking.setBookingReference(generateBookingReference());
            booking.setTicketNumber(generateTicketNumber());

           if(findPlane.getAvlSeat() - bookingdto.getBnumofseat() >= 0){
               bookingRepo.save(booking);
               findPlane.setAvlSeat(findPlane.getAvlSeat() - bookingdto.getBnumofseat());
               planeRepo.save(findPlane);
           }else {
              return ResponseEntity.badRequest().body(
                      BookingConfirmationResponse.builder()
                              .message("Seats are not available for the selected flight.")
                              .build()
              );
           }
           return ResponseEntity.ok(
                   BookingConfirmationResponse.builder()
                           .message("Demo payment accepted and your booking has been recorded.")
                           .bookingReference(booking.getBookingReference())
                           .ticketNumber(booking.getTicketNumber())
                           .startPlace(booking.getBStart())
                           .destination(booking.getBEnd())
                           .ticketCount(booking.getBNumOfseat())
                           .totalPrice(booking.getPrice())
                           .userEmail(booking.getUserEmail())
                           .build()
           );
        }else {
            return ResponseEntity.badRequest().body(
                    BookingConfirmationResponse.builder()
                            .message("The selected flight could not be found.")
                            .build()
            );
        }
    }

    private String resolveCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return "unknown-user";
        }
        return authentication.getName();
    }

    private boolean hasSameRouteEndpoints(BookingDto bookingdto) {
        if (bookingdto == null || bookingdto.getBstart() == null || bookingdto.getBend() == null) {
            return false;
        }
        return bookingdto.getBstart().trim().equalsIgnoreCase(bookingdto.getBend().trim());
    }

    private String generateBookingReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int suffix = 100000 + RANDOM.nextInt(900000);
        return String.format(Locale.ROOT, "%s-%s-%d", BOOKING_REFERENCE_PREFIX, datePart, suffix);
    }

    private String generateTicketNumber() {
        int suffix = 10000000 + RANDOM.nextInt(90000000);
        return String.format(Locale.ROOT, "%s-%d", TICKET_NUMBER_PREFIX, suffix);
    }
}
