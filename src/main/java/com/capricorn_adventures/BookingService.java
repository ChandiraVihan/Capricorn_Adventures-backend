package com.example.demo;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    // Generate unique reference ID
    private String generateReferenceId() {
        return "BK-" + UUID.randomUUID().toString().substring(0,8).toUpperCase();
    }

    // Confirm Booking
    public Booking confirmBooking(Long userId, LocalDateTime serviceDate) {
        Booking booking = new Booking();
        booking.setReferenceId(generateReferenceId());
        booking.setUserId(userId);
        booking.setBookingDate(LocalDateTime.now());
        booking.setServiceDate(serviceDate);
        booking.setStatus("CONFIRMED");

        return bookingRepository.save(booking);
    }

    // Find booking by reference ID
    public Booking findByReferenceId(String referenceId) {
        return bookingRepository.findByReferenceId(referenceId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // Get booking history
    public List<Booking> getBookingHistory(Long userId,
                                           LocalDateTime start,
                                           LocalDateTime end) {
        return bookingRepository.findByUserIdAndBookingDateBetween(userId, start, end);
    }
}