package com.pavan.arms.controller;

import com.pavan.arms.User.Role;
import com.pavan.arms.dto.Planedto;
import com.pavan.arms.entity.AirPlane;
import com.pavan.arms.entity.Booking;
import com.pavan.arms.entity.User;
import com.pavan.arms.repo.BookingRepo;
import com.pavan.arms.repo.PlaneRepo;
import com.pavan.arms.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PageController {
    private final PlaneRepo planeRepo;
    private final UserRepository userRepository;
    private final BookingRepo bookingRepo;

    @GetMapping("/")
    public String home(Model model) {
        List<AirPlane> planes = planeRepo.findAll(Sort.by(Sort.Direction.ASC, "start", "end", "id"));
        List<String> startAirports = planes.stream()
                .map(AirPlane::getStart)
                .filter(airport -> airport != null && !airport.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        Map<String, List<String>> routesByStart = planes.stream()
                .filter(plane -> plane.getStart() != null && !plane.getStart().isBlank())
                .filter(plane -> plane.getEnd() != null && !plane.getEnd().isBlank())
                .collect(Collectors.groupingBy(
                        plane -> plane.getStart().trim(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(plane -> plane.getEnd().trim(), Collectors.toList()),
                                destinations -> destinations.stream()
                                        .distinct()
                                        .sorted(String.CASE_INSENSITIVE_ORDER)
                                        .toList()
                        )
                ));
        model.addAttribute("planes", planes);
        model.addAttribute("startAirports", startAirports);
        model.addAttribute("routesByStart", routesByStart);
        return "User/index";
    }

    @GetMapping("/contact")
    public String contact() {
        return "User/contact";
    }

    @GetMapping("/select")
    public String selectFlight(Model model) {
        List<AirPlane> planes = planeRepo.findAll(Sort.by(Sort.Direction.ASC, "start", "end", "id"));
        List<String> airports = planes.stream()
                .flatMap(plane -> java.util.stream.Stream.of(plane.getStart(), plane.getEnd()))
                .filter(airport -> airport != null && !airport.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        model.addAttribute("planes", planes);
        model.addAttribute("airports", airports);
        return "User/select";
    }

    @GetMapping("/ticket")
    public String ticket() {
        return "User/ticket";
    }

    @GetMapping("/payment")
    public String payment() {
        return "User/payment";
    }

    @GetMapping("/confirmation")
    public String confirmation() {
        return "User/confirmation";
    }

    @GetMapping("/login")
    public String login() {
        return "User/login";
    }

    @GetMapping("/registration")
    public String registration() {
        return "User/registration";
    }

    @GetMapping({"/admin/dashboard", "/admin/dashboard.html"})
    public String adminDashboard(Model model) {
        populateAdminCommon(model);
        model.addAttribute("recentBookings", getRecentBookings());
        model.addAttribute("lowSeatFlights", getLowSeatFlights());
        model.addAttribute("routeSummary", getRouteSummary());
        return "Admin/dashboard";
    }

    @GetMapping({"/admin/passengers", "/admin/passenger.html", "/admin/UserDetails.html"})
    public String adminPassengers(Model model) {
        populateAdminCommon(model);
        List<User> passengers = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.ROLE_USER)
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
        model.addAttribute("passengers", passengers);
        return "Admin/UserDetails";
    }

    @GetMapping({"/admin/flights", "/admin/flights.html"})
    public String adminFlights(Model model) {
        populateAdminCommon(model);
        model.addAttribute("flights", planeRepo.findAll(Sort.by(Sort.Direction.ASC, "start", "end")));
        model.addAttribute("lowSeatFlights", getLowSeatFlights());
        return "Admin/flights";
    }

    @GetMapping({"/admin/newflight", "/admin/newflight.html"})
    public String adminNewFlight(Model model) {
        populateAdminCommon(model);
        model.addAttribute("stu", new Planedto());
        return "Admin/newflight";
    }

    @PostMapping({"/admin/newflight", "/admin/newflight.html"})
    public String createAdminFlight(@ModelAttribute("stu") Planedto planeDto, RedirectAttributes redirectAttributes) {
        AirPlane airPlane = AirPlane.builder()
                .start(planeDto.getStart())
                .end(planeDto.getEnd())
                .avlSeat(planeDto.getAvlSeat())
                .numOfKm(planeDto.getNumOfKm())
                .build();

        planeRepo.save(airPlane);
        redirectAttributes.addFlashAttribute("successMessage", "Flight added successfully for " + planeDto.getStart() + " to " + planeDto.getEnd() + ".");
        return "redirect:/admin/flights.html";
    }

    @GetMapping({"/admin/notifications", "/admin/notifications.html"})
    public String adminNotifications(Model model) {
        populateAdminCommon(model);
        List<User> newestUsers = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == Role.ROLE_USER)
                .sorted(Comparator.comparing(User::getId).reversed())
                .limit(5)
                .toList();
        model.addAttribute("recentBookings", getRecentBookings());
        model.addAttribute("lowSeatFlights", getLowSeatFlights());
        model.addAttribute("newestUsers", newestUsers);
        return "Admin/notifications";
    }

    @GetMapping({"/admin/user", "/admin/user.html"})
    public String adminUser(Model model) {
        populateAdminCommon(model);
        return "Admin/user";
    }

    @GetMapping({"/admin/paymentinfo", "/admin/paymentinfo.html"})
    public String adminPayments(Model model) {
        populateAdminCommon(model);
        model.addAttribute("bookings", getRecentBookings());
        return "Admin/paymentinfo";
    }

    private void populateAdminCommon(Model model) {
        List<Booking> bookings = bookingRepo.findAll();
        List<AirPlane> flights = planeRepo.findAll();
        List<User> users = userRepository.findAll();
        long passengerCount = users.stream()
                .filter(user -> user.getRole() == Role.ROLE_USER)
                .count();
        double totalRevenue = bookings.stream()
                .mapToDouble(Booking::getPrice)
                .sum();

        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("totalRevenue", formatCurrency(totalRevenue));
        model.addAttribute("numericRevenue", totalRevenue);
        model.addAttribute("totalPassengers", passengerCount);
        model.addAttribute("totalFlights", flights.size());
        model.addAttribute("lowSeatCount", flights.stream().filter(flight -> flight.getAvlSeat() <= 15).count());
    }

    private List<Booking> getRecentBookings() {
        return bookingRepo.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .limit(8)
                .toList();
    }

    private List<AirPlane> getLowSeatFlights() {
        return planeRepo.findAll(Sort.by(Sort.Direction.ASC, "avlSeat"))
                .stream()
                .filter(flight -> flight.getAvlSeat() <= 15)
                .limit(6)
                .toList();
    }

    private List<RouteSummary> getRouteSummary() {
        Map<String, Long> routeCount = bookingRepo.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        booking -> booking.getBStart() + " -> " + booking.getBEnd(),
                        Collectors.counting()
                ));

        return routeCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(6)
                .map(entry -> new RouteSummary(entry.getKey(), entry.getValue()))
                .toList();
    }

    private String formatCurrency(double amount) {
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return numberFormat.format(amount);
    }

    private record RouteSummary(String route, Long bookingCount) {
    }
}
