package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.AdventureBookingActionResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingDetailsDTO;
import com.capricorn_adventures.dto.AdventureRescheduleOptionsResponseDTO;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.service.ManageAdventureBookingService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManageAdventureBookingServiceImpl implements ManageAdventureBookingService {

    private static final Duration MODIFY_WINDOW = Duration.ofHours(24);

    private final AdventureCheckoutBookingRepository bookingRepository;
    private final AdventureScheduleRepository scheduleRepository;

    @Autowired
    public ManageAdventureBookingServiceImpl(AdventureCheckoutBookingRepository bookingRepository,
                                             AdventureScheduleRepository scheduleRepository) {
        this.bookingRepository = bookingRepository;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public AdventureBookingDetailsDTO getAdventureBookingDetails(UUID userId, Long bookingId) {
        AdventureCheckoutBooking booking = findOwnedBooking(userId, bookingId);
        return mapDetails(booking);
    }

    @Override
    public AdventureRescheduleOptionsResponseDTO getRescheduleOptions(UUID userId, Long bookingId) {
        AdventureCheckoutBooking booking = findOwnedBooking(userId, bookingId);
        AdventureRescheduleOptionsResponseDTO response = new AdventureRescheduleOptionsResponseDTO();
        response.setCurrentScheduleId(booking.getSchedule().getId());

        String restriction = getModifyRestriction(booking);
        if (restriction != null) {
            response.setAllowed(false);
            response.setMessage(restriction);
            response.setAvailableSlots(List.of());
            return response;
        }

        List<AdventureSchedule> options = scheduleRepository.findRescheduleOptions(
                booking.getAdventure().getId(),
                booking.getSchedule().getId(),
                booking.getParticipants(),
                LocalDateTime.now()
        );

        response.setAllowed(true);
        response.setMessage(options.isEmpty() ? "No alternative slots available" : null);
        response.setAvailableSlots(options.stream().map(this::mapScheduleOption).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional
    public AdventureBookingActionResponseDTO rescheduleBooking(UUID userId, Long bookingId, Long newScheduleId) {
        AdventureCheckoutBooking booking = bookingRepository.findByIdAndUserIdForUpdateWithDetails(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure booking not found with ID: " + bookingId));

        String restriction = getModifyRestriction(booking);
        if (restriction != null) {
            throw new BadRequestException(restriction);
        }

        if (newScheduleId == null) {
            throw new BadRequestException("newScheduleId is required");
        }

        if (newScheduleId.equals(booking.getSchedule().getId())) {
            throw new BadRequestException("Selected slot is already assigned to this booking");
        }

        AdventureSchedule oldSchedule = scheduleRepository.findByIdForUpdateWithAdventure(booking.getSchedule().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current schedule not found"));
        AdventureSchedule newSchedule = scheduleRepository.findByIdForUpdateWithAdventure(newScheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with ID: " + newScheduleId));

        if (!newSchedule.getAdventure().getId().equals(booking.getAdventure().getId())) {
            throw new BadRequestException("Selected schedule does not belong to this adventure");
        }

        if (newSchedule.getStartDate() == null || newSchedule.getStartDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Selected schedule is not in the future");
        }

        if (!"AVAILABLE".equalsIgnoreCase(newSchedule.getStatus())) {
            throw new BadRequestException("Selected schedule is not available for rescheduling");
        }

        int newAvailableSlots = newSchedule.getAvailableSlots() == null ? 0 : newSchedule.getAvailableSlots();
        if (newAvailableSlots < booking.getParticipants()) {
            throw new BadRequestException("Selected schedule does not have enough capacity");
        }

        // Release old schedule capacity, then reserve on new schedule.
        oldSchedule.setAvailableSlots((oldSchedule.getAvailableSlots() == null ? 0 : oldSchedule.getAvailableSlots())
                + booking.getParticipants());
        if ("SOLD_OUT".equalsIgnoreCase(oldSchedule.getStatus())) {
            oldSchedule.setStatus("AVAILABLE");
        }

        newSchedule.setAvailableSlots(newAvailableSlots - booking.getParticipants());
        if (newSchedule.getAvailableSlots() == 0) {
            newSchedule.setStatus("SOLD_OUT");
        }

        booking.setSchedule(newSchedule);
        scheduleRepository.save(oldSchedule);
        scheduleRepository.save(newSchedule);
        bookingRepository.save(booking);

        AdventureBookingActionResponseDTO response = new AdventureBookingActionResponseDTO();
        response.setBookingId(booking.getId());
        response.setStatus(booking.getStatus().name());
        response.setScheduleId(newSchedule.getId());
        response.setStartDateTime(newSchedule.getStartDate());
        response.setEndDateTime(newSchedule.getEndDate());
        response.setMessage("Booking rescheduled successfully");
        return response;
    }

    @Override
    @Transactional
    public AdventureBookingActionResponseDTO cancelBooking(UUID userId, Long bookingId) {
        AdventureCheckoutBooking booking = bookingRepository.findByIdAndUserIdForUpdateWithDetails(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure booking not found with ID: " + bookingId));

        String restriction = getModifyRestriction(booking);
        if (restriction != null) {
            throw new BadRequestException(restriction);
        }

        AdventureSchedule lockedSchedule = scheduleRepository.findByIdForUpdateWithAdventure(booking.getSchedule().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current schedule not found"));

        lockedSchedule.setAvailableSlots((lockedSchedule.getAvailableSlots() == null ? 0 : lockedSchedule.getAvailableSlots())
                + booking.getParticipants());
        if ("SOLD_OUT".equalsIgnoreCase(lockedSchedule.getStatus())) {
            lockedSchedule.setStatus("AVAILABLE");
        }

        booking.setStatus(AdventureCheckoutStatus.CANCELLED);
        scheduleRepository.save(lockedSchedule);
        bookingRepository.save(booking);

        AdventureBookingActionResponseDTO response = new AdventureBookingActionResponseDTO();
        response.setBookingId(booking.getId());
        response.setStatus(booking.getStatus().name());
        response.setScheduleId(booking.getSchedule().getId());
        response.setStartDateTime(booking.getSchedule().getStartDate());
        response.setEndDateTime(booking.getSchedule().getEndDate());
        response.setMessage("Booking cancelled successfully and slot capacity released");
        return response;
    }

    private AdventureCheckoutBooking findOwnedBooking(UUID userId, Long bookingId) {
        return bookingRepository.findByIdAndUserIdWithDetails(bookingId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure booking not found with ID: " + bookingId));
    }

    private String getModifyRestriction(AdventureCheckoutBooking booking) {
        if (booking.getStatus() != AdventureCheckoutStatus.CONFIRMED) {
            return "Only confirmed bookings can be changed";
        }

        LocalDateTime start = booking.getSchedule().getStartDate();
        if (start == null || !start.isAfter(LocalDateTime.now().plus(MODIFY_WINDOW))) {
            return "Reschedule/cancel is not allowed within 24 hours of the adventure start time";
        }

        return null;
    }

    private AdventureBookingDetailsDTO mapDetails(AdventureCheckoutBooking booking) {
        AdventureBookingDetailsDTO dto = new AdventureBookingDetailsDTO();
        dto.setBookingId(booking.getId());
        dto.setBookingReference(booking.getBookingReference());
        dto.setAdventureName(booking.getAdventure().getName());
        dto.setStartDateTime(booking.getSchedule().getStartDate());
        dto.setEndDateTime(booking.getSchedule().getEndDate());
        dto.setParticipants(booking.getParticipants());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus().name());
        dto.setProviderInfo("Capricorn Adventures");
        dto.setMeetingPoint(booking.getMeetingPoint());
        dto.setWhatToBring(parseWhatToBring(booking.getWhatToBring()));

        String restriction = getModifyRestriction(booking);
        dto.setRescheduleAllowed(restriction == null);
        dto.setCancelAllowed(restriction == null);
        dto.setRestrictionMessage(restriction);
        return dto;
    }

    private AdventureRescheduleOptionsResponseDTO.ScheduleOptionDTO mapScheduleOption(AdventureSchedule schedule) {
        AdventureRescheduleOptionsResponseDTO.ScheduleOptionDTO dto =
                new AdventureRescheduleOptionsResponseDTO.ScheduleOptionDTO();
        dto.setScheduleId(schedule.getId());
        dto.setStartDateTime(schedule.getStartDate());
        dto.setEndDateTime(schedule.getEndDate());
        dto.setAvailableSlots(schedule.getAvailableSlots());
        return dto;
    }

    private List<String> parseWhatToBring(String inclusions) {
        if (inclusions == null || inclusions.isBlank()) {
            return List.of();
        }
        return Arrays.stream(inclusions.split(",|\\n"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
