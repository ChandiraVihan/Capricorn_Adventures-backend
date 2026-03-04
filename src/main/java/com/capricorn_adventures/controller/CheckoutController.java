package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.GuestDetailsDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
public class CheckoutController {

    private final BookingRepository bookingRepository;

    @Autowired
    public CheckoutController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Update guest details for a pending booking
    @PutMapping("/{id}/guest")
    public ResponseEntity<?> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestDetailsDTO guestDTO) {

        Optional<Booking> optionalBooking = bookingRepository.findById(id);

        if(optionalBooking.isEmpty()) {
            return ResponseEntity.badRequest().body("Booking not found");
        }

        Booking booking = optionalBooking.get();
        booking.setGuestName(guestDTO.getName());
        booking.setGuestEmail(guestDTO.getEmail());
        booking.setGuestPhone(guestDTO.getPhone());

        bookingRepository.save(booking);

        return ResponseEntity.ok("Guest details updated");
    }

    // Confirm payment
    @PostMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(
            @PathVariable Long id,
            @RequestParam boolean paymentSuccess) {

        Optional<Booking> optionalBooking = bookingRepository.findById(id);

        if(optionalBooking.isEmpty()) {
            return ResponseEntity.badRequest().body("Booking not found");
        }

        Booking booking = optionalBooking.get();
        if (booking.getStatus() == com.capricorn_adventures.entity.BookingStatus.CONFIRMED) {
             return ResponseEntity.badRequest().body("Booking is already confirmed");
        }

        if(paymentSuccess) {
            booking.setStatus(com.capricorn_adventures.entity.BookingStatus.CONFIRMED);
            String reference = "CAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            booking.setReferenceId(reference);
            bookingRepository.save(booking);
            return ResponseEntity.ok(booking);
        } else {
            // We don't necessarily CANCEL it immediately to allow retry, but let's follow the simple mock
            booking.setStatus(com.capricorn_adventures.entity.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            return ResponseEntity.badRequest().body("Payment failed. Please try again.");
        }
    }
}
