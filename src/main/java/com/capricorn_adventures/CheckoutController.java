package com.example.us07;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin
public class CheckoutController {

    @Autowired
    private BookingRepository bookingRepository;

    // Create booking (Start checkout)
    @PostMapping("/create")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        booking.setStatus("PENDING");
        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    // Update guest details
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

        if(paymentSuccess) {
            booking.setStatus("CONFIRMED");
            booking.generateReference();
        } else {
            booking.setStatus("FAILED");
        }

        bookingRepository.save(booking);

        return ResponseEntity.ok(booking);
    }
}