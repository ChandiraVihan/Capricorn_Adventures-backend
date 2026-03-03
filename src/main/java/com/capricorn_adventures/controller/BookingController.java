package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*") // Adjust origin as needed for your frontend
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
}
