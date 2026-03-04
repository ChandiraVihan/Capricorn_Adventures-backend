package com.example.demo;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // Confirm booking
    @PostMapping("/confirm")
    public Booking confirmBooking(@RequestParam Long userId,
                                  @RequestParam String serviceDate) {
        return bookingService.confirmBooking(
                userId,
                LocalDateTime.parse(serviceDate)
        );
    }

    // Find booking by reference ID
    @GetMapping("/find/{referenceId}")
    public Booking findBooking(@PathVariable String referenceId) {
        return bookingService.findByReferenceId(referenceId);
    }

    // Booking history with filter
    @GetMapping("/history")
    public List<Booking> getHistory(@RequestParam Long userId,
                                    @RequestParam String startDate,
                                    @RequestParam String endDate) {

        return bookingService.getBookingHistory(
                userId,
                LocalDateTime.parse(startDate),
                LocalDateTime.parse(endDate)
        );
    }
}