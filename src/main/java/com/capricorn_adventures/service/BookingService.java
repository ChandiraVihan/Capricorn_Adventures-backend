package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    Booking createBooking(BookingRequestDTO bookingRequestDTO);
    Booking confirmBooking(Long bookingId);
    Booking findBooking(String referenceId);
    List<Booking> getBookingHistory(LocalDate startDate, LocalDate endDate);
}
