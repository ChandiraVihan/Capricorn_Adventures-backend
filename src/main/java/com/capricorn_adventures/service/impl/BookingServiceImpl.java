package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.Room;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.exception.RoomUnavailableException;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.RoomRepository;
import com.capricorn_adventures.service.BookingService;
import com.capricorn_adventures.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        // 1. Check if room exists
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        // 2. Validate availability based on dates
        boolean available = roomService.isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!available) {
            throw new RoomUnavailableException("Room is not available for the selected dates!");
        }

        // 3. Create the initial Booking (PENDING state)
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.PENDING); // Assuming default is PENDING before payment

        return bookingRepository.save(booking);
    }
}
