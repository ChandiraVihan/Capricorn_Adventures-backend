package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.service.BookingService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*") 
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequestDTO bookingRequestDTO) {
        Booking createdBooking = bookingService.createBooking(bookingRequestDTO);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Booking> confirmBooking(@RequestBody Map<String, Long> payload) {
        Long bookingId = payload.get("bookingId");
        if (bookingId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    @GetMapping("/find/{referenceId}")
    public ResponseEntity<Booking> findBooking(@PathVariable String referenceId) {
        return ResponseEntity.ok(bookingService.findBooking(referenceId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Booking>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(bookingService.getBookingHistory(startDate, endDate));
    }
}
