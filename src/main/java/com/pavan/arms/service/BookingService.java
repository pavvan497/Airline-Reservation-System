package com.pavan.arms.service;


import com.pavan.arms.dto.BookingDto;
import com.pavan.arms.dto.BookingActionResponse;
import com.pavan.arms.dto.BookingConfirmationResponse;
import com.pavan.arms.entity.AirPlane;
import com.pavan.arms.entity.Booking;
import com.pavan.arms.entity.BookingStatus;
import com.pavan.arms.repo.BookingRepo;
import com.pavan.arms.repo.PlaneRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {
    private static final double BASE_FARE_PER_KM = 10.0;
    private static final String BOOKING_REFERENCE_PREFIX = "ARMS";
    private static final String TICKET_NUMBER_PREFIX = "TKT";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final PlaneRepo planeRepo;
    private final BookingRepo bookingRepo;


    public List<Booking> getAllBooking() {
        return bookingRepo.findAll();
    }

    public List<Booking> getCurrentUserBookings() {
        String currentUserEmail = resolveCurrentUserEmail();
        return bookingRepo.findByUserEmailOrderByTravelDateDescIdDesc(currentUserEmail)
                .stream()
                .sorted(Comparator
                        .comparing(Booking::getTravelDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Booking::getId, Comparator.reverseOrder()))
                .toList();
    }


    public ResponseEntity<Double> checkPrice(BookingDto bookingdto) {
        if (hasSameRouteEndpoints(bookingdto)) {
            return ResponseEntity.badRequest().build();
        }
        if (!isValidTravelDate(bookingdto)) {
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

    @Transactional
    public ResponseEntity<BookingConfirmationResponse> addBooking(BookingDto bookingdto) {
        if (hasSameRouteEndpoints(bookingdto)) {
            return ResponseEntity.badRequest().body(
                    BookingConfirmationResponse.builder()
                            .message("Source and destination cannot be the same.")
                            .build()
            );
        }
        if (!isValidTravelDate(bookingdto)) {
            return ResponseEntity.badRequest().body(
                    BookingConfirmationResponse.builder()
                            .message("Please choose a valid travel date for today or later.")
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
            booking.setTravelDate(bookingdto.getTravelDate());
            booking.setUserEmail(resolveCurrentUserEmail());
            booking.setBookingReference(generateBookingReference());
            booking.setTicketNumber(generateTicketNumber());
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setCancelledAt(null);

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
                           .travelDate(booking.getTravelDate())
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

    @Transactional
    public ResponseEntity<BookingActionResponse> cancelBooking(int bookingId) {
        Booking booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        String currentUserEmail = resolveCurrentUserEmail();
        if (booking.getUserEmail() == null || !booking.getUserEmail().equalsIgnoreCase(currentUserEmail)) {
            return ResponseEntity.status(403).body(
                    BookingActionResponse.builder()
                            .message("You can only cancel your own bookings.")
                            .bookingId(bookingId)
                            .build()
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return ResponseEntity.badRequest().body(
                    BookingActionResponse.builder()
                            .message("This booking has already been cancelled.")
                            .bookingId(bookingId)
                            .bookingStatus(BookingStatus.CANCELLED.name())
                            .build()
            );
        }

        if (booking.getTravelDate() != null && booking.getTravelDate().isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(
                    BookingActionResponse.builder()
                            .message("Past flights cannot be cancelled.")
                            .bookingId(bookingId)
                            .bookingStatus(booking.getStatus() == null ? BookingStatus.CONFIRMED.name() : booking.getStatus().name())
                            .build()
            );
        }

        AirPlane flight = planeRepo.findByStartAndEndIgnoreCase(booking.getBStart(), booking.getBEnd());
        if (flight != null) {
            flight.setAvlSeat(flight.getAvlSeat() + booking.getBNumOfseat());
            planeRepo.save(flight);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepo.save(booking);

        return ResponseEntity.ok(
                BookingActionResponse.builder()
                        .message("Your booking has been cancelled successfully.")
                        .bookingId(bookingId)
                        .bookingStatus(BookingStatus.CANCELLED.name())
                        .build()
        );
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

    private boolean isValidTravelDate(BookingDto bookingdto) {
        if (bookingdto == null || bookingdto.getTravelDate() == null) {
            return false;
        }
        return !bookingdto.getTravelDate().isBefore(LocalDate.now());
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
