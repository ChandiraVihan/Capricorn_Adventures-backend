package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.dto.RoomBookingDetailsDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.BookingService;
import com.capricorn_adventures.service.CancellationPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final CancellationPolicyService cancellationPolicyService;

    @Autowired
    public BookingController(BookingService bookingService, 
                             UserRepository userRepository,
                             CancellationPolicyService cancellationPolicyService) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.cancellationPolicyService = cancellationPolicyService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO) {
        Booking createdBooking = bookingService.createBooking(bookingRequestDTO);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookingController.class);

    @GetMapping("/reference/{ref}")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getBookingByReference(@PathVariable String ref) {
        log.info("Received request to track booking with reference: {}", ref);
        return bookingService.getBookingByReference(ref)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @DeleteMapping("/reference/{ref}")
    public ResponseEntity<?> cancelBooking(@PathVariable String ref) {
        bookingService.cancelBooking(ref);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getBookingDetails(Authentication auth, @PathVariable Long id) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID userId = UUID.fromString(auth.getName());
            Booking booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new Exception("Booking not found"));

            if (!booking.getUser().getId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            RoomBookingDetailsDTO details = new RoomBookingDetailsDTO();
            details.setId(booking.getId());
            details.setReferenceId(booking.getReferenceId());
            details.setRoomName(booking.getRoom() != null ? booking.getRoom().getName() : "Exclusive Suite");
            details.setCheckInDate(booking.getCheckInDate());
            details.setCheckOutDate(booking.getCheckOutDate());
            details.setGuestName(booking.getGuestName());
            details.setGuestEmail(booking.getGuestEmail());
            details.setGuestPhone(booking.getGuestPhone());
            details.setTotalPrice(booking.getTotalPrice());
            details.setStatus(booking.getStatus().name());
            
            // Logic for allowing cancellation
            boolean canCancel = booking.getStatus() == BookingStatus.CONFIRMED;
            details.setCancelAllowed(canCancel);
            if (!canCancel) {
                details.setRestrictionMessage("This booking cannot be cancelled in its current state.");
            }

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<?> getMyBookings(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            UUID userId = UUID.fromString(auth.getName());
            User user = userRepository.findById(userId).orElseThrow();
            List<Booking> bookings = bookingService.getUserBookings(user);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
