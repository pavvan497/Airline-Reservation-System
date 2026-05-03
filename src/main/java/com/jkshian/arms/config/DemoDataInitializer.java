package com.jkshian.arms.config;

import com.jkshian.arms.User.Role;
import com.jkshian.arms.entity.AirPlane;
import com.jkshian.arms.entity.Booking;
import com.jkshian.arms.entity.User;
import com.jkshian.arms.repo.BookingRepo;
import com.jkshian.arms.repo.PlaneRepo;
import com.jkshian.arms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {
    private static final double LEGACY_INVALID_PRICE_THRESHOLD = 1_000_000.0;
    private final PlaneRepo planeRepo;
    private final BookingRepo bookingRepo;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        cleanupLegacyBookings();
        seedFlights();
        seedUsers();
    }

    private void cleanupLegacyBookings() {
        List<Booking> legacyBookings = bookingRepo.findAll()
                .stream()
                .filter(this::isLegacyInvalidBooking)
                .toList();

        for (Booking booking : legacyBookings) {
            AirPlane flight = planeRepo.findByStartAndEndIgnoreCase(booking.getBStart(), booking.getBEnd());
            if (flight != null) {
                flight.setAvlSeat(flight.getAvlSeat() + booking.getBNumOfseat());
                planeRepo.save(flight);
            }
        }

        if (!legacyBookings.isEmpty()) {
            bookingRepo.deleteAllInBatch(legacyBookings);
        }
    }

    private boolean isLegacyInvalidBooking(Booking booking) {
        return booking.getPrice() >= LEGACY_INVALID_PRICE_THRESHOLD
                && (booking.getBookingReference() == null || booking.getTicketNumber() == null);
    }

    private void seedFlights() {
        List<AirPlane> demoFlights = List.of(
                createFlight("Delhi", "Mumbai", 120, 1400),
                createFlight("Mumbai", "Delhi", 120, 1400),
                createFlight("Delhi", "Bengaluru", 90, 1740),
                createFlight("Bengaluru", "Delhi", 90, 1740),
                createFlight("Chennai", "Hyderabad", 80, 630),
                createFlight("Hyderabad", "Chennai", 80, 630),
                createFlight("Kolkata", "Delhi", 110, 1530),
                createFlight("Delhi", "Kolkata", 110, 1530),
                createFlight("Pune", "Ahmedabad", 75, 660),
                createFlight("Ahmedabad", "Pune", 75, 660),
                createFlight("Goa", "Mumbai", 70, 590),
                createFlight("Mumbai", "Goa", 70, 590),
                createFlight("Jaipur", "Delhi", 65, 280),
                createFlight("Delhi", "Jaipur", 65, 280),
                createFlight("Lucknow", "Delhi", 85, 500),
                createFlight("Delhi", "Lucknow", 85, 500),
                createFlight("Patna", "Kolkata", 60, 580),
                createFlight("Kolkata", "Patna", 60, 580),
                createFlight("Bengaluru", "Chennai", 95, 350),
                createFlight("Chennai", "Bengaluru", 95, 350)
        );

        for (AirPlane flight : demoFlights) {
            if (!planeRepo.existsByStartIgnoreCaseAndEndIgnoreCase(flight.getStart(), flight.getEnd())) {
                planeRepo.save(flight);
            }
        }
    }

    private void seedUsers() {
        if (userRepository.findByEmail("demo.user@arms.com").isEmpty()) {
            userRepository.save(User.builder()
                    .firstName("Demo")
                    .lastName("User")
                    .email("demo.user@arms.com")
                    .password(passwordEncoder.encode("password123"))
                    .role(Role.ROLE_USER)
                    .build());
        }

        if (userRepository.findByEmail("admin@arms.com").isEmpty()) {
            userRepository.save(User.builder()
                    .firstName("Demo")
                    .lastName("Admin")
                    .email("admin@arms.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .build());
        }
    }

    private AirPlane createFlight(String start, String end, int seats, double km) {
        return AirPlane.builder()
                .start(start)
                .end(end)
                .avlSeat(seats)
                .numOfKm(km)
                .build();
    }
}
