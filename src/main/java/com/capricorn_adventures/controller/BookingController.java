package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.BookingDetailDto;
import com.capricorn_adventures.dto.BookingSummaryDto;
import com.capricorn_adventures.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public ResponseEntity<Map<String, List<BookingSummaryDto>>> getUserBookings(@RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDetailDto> getBookingDetails(
            @PathVariable Long id,
            @RequestParam Long userId) {
        return ResponseEntity.ok(bookingService.getBookingDetails(id, userId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelBooking(
            @PathVariable Long id,
            @RequestParam Long userId) {
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully."));
    }
}
