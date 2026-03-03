package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;

public interface BookingService {
    Booking createBooking(BookingRequestDTO bookingRequestDTO);
}
