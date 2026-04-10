package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.User;

import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(BookingRequestDTO bookingRequestDTO);
    Optional<Booking> getBookingByReference(String referenceId);
    Optional<Booking> getBookingById(Long id);
    List<Booking> getUserBookings(User user);
    void cancelBooking(String referenceId);
}
