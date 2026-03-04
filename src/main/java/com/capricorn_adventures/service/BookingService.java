package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.BookingDetailDto;
import com.capricorn_adventures.dto.BookingSummaryDto;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.exception.BookingNotFoundException;
import com.capricorn_adventures.exception.CancellationNotAllowedException;
import com.capricorn_adventures.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public Map<String, List<BookingSummaryDto>> getUserBookings(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCheckInDateDesc(userId);

        List<BookingSummaryDto> past = bookings.stream()
                .filter(b -> b.getCheckOutDate().isBefore(now))
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        List<BookingSummaryDto> upcoming = bookings.stream()
                .filter(b -> !b.getCheckOutDate().isBefore(now))
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        return Map.of("past", past, "upcoming", upcoming);
    }

    @Transactional(readOnly = true)
    public BookingDetailDto getBookingDetails(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found."));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingNotFoundException("Booking not found.");
        }

        return mapToDetailDto(booking);
    }

    @Transactional
    public void cancelBooking(Long id, Long userId) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found."));

        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingNotFoundException("Booking not found.");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new CancellationNotAllowedException("Booking is already cancelled.");
        }

        if (!booking.isRefundable()) {
            throw new CancellationNotAllowedException("Booking is non-refundable and cannot be cancelled.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getCancellationDeadline() != null && now.isAfter(booking.getCancellationDeadline())) {
            throw new CancellationNotAllowedException("Cancellation window has expired.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        try {
            emailService.sendCancellationEmail(booking);
        } catch (Exception e) {
            // Log exception but don't fail booking cancellation
            // Real implementation would log or queue the failure
        }
    }

    private BookingSummaryDto mapToSummaryDto(Booking booking) {
        BookingSummaryDto dto = new BookingSummaryDto();
        dto.setId(booking.getId());
        dto.setHotelName(booking.getHotel().getName());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setStatus(booking.getStatus());
        return dto;
    }

    private BookingDetailDto mapToDetailDto(Booking booking) {
        BookingDetailDto dto = new BookingDetailDto();
        dto.setId(booking.getId());
        dto.setHotelName(booking.getHotel().getName());
        dto.setHotelAddress(booking.getHotel().getAddress());
        dto.setCheckInDate(booking.getCheckInDate());
        dto.setCheckOutDate(booking.getCheckOutDate());
        dto.setRoomType(booking.getRoomType());
        dto.setGuestsCount(booking.getGuestsCount());
        dto.setPricePaid(booking.getPricePaid());
        dto.setStatus(booking.getStatus());
        dto.setRefundable(booking.isRefundable());
        dto.setCancellationDeadline(booking.getCancellationDeadline());
        dto.setGuestName(booking.getUser().getName());
        return dto;
    }
}
