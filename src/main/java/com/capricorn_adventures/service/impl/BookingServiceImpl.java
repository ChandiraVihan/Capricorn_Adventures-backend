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
import com.capricorn_adventures.service.EmailService;
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
import java.security.SecureRandom;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final RoomService roomService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, 
                          RoomRepository roomRepository, 
                          RoomService roomService, 
                          UserRepository userRepository,
                          EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.roomService = roomService;
        this.userRepository = userRepository;
        this.emailService = emailService;
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
        booking.setStatus(BookingStatus.CONFIRMED); // Direct confirmation for now
        
        // Generate Reference ID (e.g., CAP-XXXXXX)
        booking.setReferenceId(generateReferenceId());

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) nights = 1;
        BigDecimal totalPrice = room.getBasePrice().multiply(BigDecimal.valueOf(nights));
        booking.setTotalPrice(totalPrice);

        // Associate with User if authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User) {
                booking.setUser((User) principal);
            } else if (!principal.equals("anonymousUser")) {
                try {
                    UUID userId = UUID.fromString(auth.getName());
                    userRepository.findById(userId).ifPresent(booking::setUser);
                } catch (Exception ignored) {
                    // Log or handle the case where principal name is not a UUID
                }
            }
        }

        Booking savedBooking = bookingRepository.save(booking);
        
        // Send Email Notification
        emailService.sendBookingConfirmation(savedBooking);

        return savedBooking;
    }

    private String generateReferenceId() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("CAP-");
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public Optional<Booking> getBookingByReference(String referenceId) {
        return bookingRepository.findByReferenceId(referenceId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void cancelBooking(String referenceId) {
        bookingRepository.deleteByReferenceId(referenceId);
    }

    @Override
    public List<Booking> getUserBookings(User user) {
        return bookingRepository.findByUserOrderByCheckInDateDesc(user);
    }
}
