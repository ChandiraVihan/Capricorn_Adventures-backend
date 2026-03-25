package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.AdventureBookingActionResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingDetailsDTO;
import com.capricorn_adventures.dto.AdventureRescheduleOptionsResponseDTO;
import com.capricorn_adventures.dto.AdventureRescheduleRequestDTO;
import com.capricorn_adventures.dto.MyBookingListItemDTO;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.ManageAdventureBookingService;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/my-bookings")
@CrossOrigin(origins = "*")
public class MyAdventureBookingsController {

    private final BookingRepository bookingRepository;
    private final AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;
    private final ManageAdventureBookingService manageAdventureBookingService;
    private final UserRepository userRepository;

    @Autowired
    public MyAdventureBookingsController(BookingRepository bookingRepository,
                                         AdventureCheckoutBookingRepository adventureCheckoutBookingRepository,
                                         ManageAdventureBookingService manageAdventureBookingService,
                                         UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.adventureCheckoutBookingRepository = adventureCheckoutBookingRepository;
        this.manageAdventureBookingService = manageAdventureBookingService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getMyBookings(Authentication authentication,
                                           @RequestParam(required = false) String type) {
        User user = resolveCustomer(authentication);
        String normalized = type == null ? null : type.trim().toUpperCase(Locale.ROOT);

        if (normalized != null && !normalized.equals("HOTEL") && !normalized.equals("ADVENTURE")) {
            return ResponseEntity.badRequest().body("type must be HOTEL or ADVENTURE");
        }

        List<MyBookingListItemDTO> response = new ArrayList<>();
        if (normalized == null || normalized.equals("HOTEL")) {
            for (Booking booking : bookingRepository.findByUserOrderByCheckInDateDesc(user)) {
                MyBookingListItemDTO item = new MyBookingListItemDTO();
                item.setType("HOTEL");
                item.setBookingId(booking.getId());
                item.setTitle(booking.getRoom() == null ? "Hotel booking" : booking.getRoom().getName());
                item.setStatus(booking.getStatus() == null ? null : booking.getStatus().name());
                item.setDate(booking.getCheckInDate());
                item.setParticipants(null);
                item.setTotalPrice(booking.getTotalPrice());
                response.add(item);
            }
        }

        if (normalized == null || normalized.equals("ADVENTURE")) {
            for (AdventureCheckoutBooking booking : adventureCheckoutBookingRepository
                    .findByUserIdWithDetailsOrderByCreatedAtDesc(user.getId())) {
                MyBookingListItemDTO item = new MyBookingListItemDTO();
                item.setType("ADVENTURE");
                item.setBookingId(booking.getId());
                item.setTitle(booking.getAdventure().getName());
                item.setStatus(booking.getStatus().name());
                item.setDate(booking.getSchedule().getStartDate() == null
                        ? null
                        : booking.getSchedule().getStartDate().toLocalDate());
                item.setStartDateTime(booking.getSchedule().getStartDate());
                item.setEndDateTime(booking.getSchedule().getEndDate());
                item.setParticipants(booking.getParticipants());
                item.setTotalPrice(booking.getTotalPrice());
                response.add(item);
            }
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/adventure/{bookingId}")
    public ResponseEntity<?> getAdventureBookingDetails(Authentication authentication,
                                                        @PathVariable Long bookingId) {
        User user = resolveCustomer(authentication);
        AdventureBookingDetailsDTO details = manageAdventureBookingService.getAdventureBookingDetails(user.getId(), bookingId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/adventure/{bookingId}/reschedule-options")
    public ResponseEntity<?> getRescheduleOptions(Authentication authentication,
                                                  @PathVariable Long bookingId) {
        User user = resolveCustomer(authentication);
        AdventureRescheduleOptionsResponseDTO options =
                manageAdventureBookingService.getRescheduleOptions(user.getId(), bookingId);
        return ResponseEntity.ok(options);
    }

    @PutMapping("/adventure/{bookingId}/reschedule")
    public ResponseEntity<?> rescheduleBooking(Authentication authentication,
                                               @PathVariable Long bookingId,
                                               @Valid @RequestBody AdventureRescheduleRequestDTO request) {
        User user = resolveCustomer(authentication);
        AdventureBookingActionResponseDTO response =
                manageAdventureBookingService.rescheduleBooking(user.getId(), bookingId, request.getNewScheduleId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/adventure/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(Authentication authentication,
                                           @PathVariable Long bookingId) {
        User user = resolveCustomer(authentication);
        AdventureBookingActionResponseDTO response =
                manageAdventureBookingService.cancelBooking(user.getId(), bookingId);
        return ResponseEntity.ok(response);
    }

    private User resolveCustomer(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Object principal = authentication.getPrincipal();
        User user;
        if (principal instanceof User principalUser) {
            user = principalUser;
        } else {
            UUID userId;
            try {
                String principalName = authentication.getName();
                if (principal instanceof UserDetails userDetails) {
                    principalName = userDetails.getUsername();
                }
                userId = UUID.fromString(principalName);
            } catch (Exception ex) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication context");
            }

            user = userRepository.findById(userId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        }

        if (user.getRole() != User.UserRole.CUSTOMER) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only CUSTOMER role can manage personal adventure bookings");
        }
        return user;
    }
}
