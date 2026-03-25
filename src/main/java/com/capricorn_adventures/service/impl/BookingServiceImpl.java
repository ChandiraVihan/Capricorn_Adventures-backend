package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.BookingRequestDTO;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.Room;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.exception.RoomUnavailableException;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.RoomRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.BookingService;
import com.capricorn_adventures.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, RoomService roomService, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + request.getRoomId()));

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date!");
        }

        boolean available = roomService.isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());
        if (!available) {
            throw new RoomUnavailableException("Room is not available for the selected dates!");
        }

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setStatus(BookingStatus.PENDING);

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;
        BigDecimal totalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(nights));
        booking.setTotalPrice(totalPrice);

        // Associate with User if authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            try {
                // Assuming Name is the User ID (UUID) as set in JwtFilter or similar
                UUID userId = UUID.fromString(auth.getName());
                userRepository.findById(userId).ifPresent(booking::setUser);
            } catch (Exception ignored) {}
        }

        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> getBookingByReference(String referenceId) {
        return bookingRepository.findByReferenceId(referenceId);
    }

    @Override
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCheckInDateDesc(user);
    }
}
