package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.AdventureBookingActionResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingDetailsDTO;
import com.capricorn_adventures.dto.AdventureRescheduleOptionsResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageAdventureBookingServiceImplTest {

    @Mock
    private AdventureCheckoutBookingRepository bookingRepository;

    @Mock
    private AdventureScheduleRepository scheduleRepository;

    private ManageAdventureBookingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManageAdventureBookingServiceImpl(bookingRepository, scheduleRepository);
    }

    @Test
    void getAdventureBookingDetails_returnsExpectedBookingData() {
        UUID userId = UUID.randomUUID();
        AdventureCheckoutBooking booking = confirmedBooking(101L, 3, 48, 6);
        when(bookingRepository.findByIdAndUserIdWithDetails(101L, userId)).thenReturn(Optional.of(booking));

        AdventureBookingDetailsDTO details = service.getAdventureBookingDetails(userId, 101L);

        assertEquals(101L, details.getBookingId());
        assertEquals("Island Safari", details.getAdventureName());
        assertEquals(3, details.getParticipants());
        assertTrue(details.isRescheduleAllowed());
        assertTrue(details.isCancelAllowed());
    }

    @Test
    void getRescheduleOptions_returnsRestrictionWhenPolicyWindowViolated() {
        UUID userId = UUID.randomUUID();
        AdventureCheckoutBooking booking = confirmedBooking(102L, 2, 8, 4);
        when(bookingRepository.findByIdAndUserIdWithDetails(102L, userId)).thenReturn(Optional.of(booking));

        AdventureRescheduleOptionsResponseDTO response = service.getRescheduleOptions(userId, 102L);

        assertFalse(response.isAllowed());
        assertTrue(response.getMessage().contains("24 hours"));
    }

    @Test
    void rescheduleBooking_updatesBookingToNewScheduleWhenAllowed() {
        UUID userId = UUID.randomUUID();
        AdventureCheckoutBooking booking = confirmedBooking(103L, 2, 72, 4);
        AdventureSchedule oldSchedule = booking.getSchedule();
        AdventureSchedule newSchedule = schedule(88L, booking.getAdventure(), 10, 96);

        when(bookingRepository.findByIdAndUserIdForUpdateWithDetails(103L, userId)).thenReturn(Optional.of(booking));
        when(scheduleRepository.findByIdForUpdateWithAdventure(oldSchedule.getId())).thenReturn(Optional.of(oldSchedule));
        when(scheduleRepository.findByIdForUpdateWithAdventure(88L)).thenReturn(Optional.of(newSchedule));
        when(scheduleRepository.save(any(AdventureSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(AdventureCheckoutBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdventureBookingActionResponseDTO response = service.rescheduleBooking(userId, 103L, 88L);

        assertEquals(88L, response.getScheduleId());
        assertEquals("Booking rescheduled successfully", response.getMessage());
        assertEquals(8, newSchedule.getAvailableSlots());
    }

    @Test
    void cancelBooking_releasesCapacityAndSetsCancelledStatus() {
        UUID userId = UUID.randomUUID();
        AdventureCheckoutBooking booking = confirmedBooking(104L, 2, 72, 4);
        AdventureSchedule schedule = booking.getSchedule();

        when(bookingRepository.findByIdAndUserIdForUpdateWithDetails(104L, userId)).thenReturn(Optional.of(booking));
        when(scheduleRepository.findByIdForUpdateWithAdventure(schedule.getId())).thenReturn(Optional.of(schedule));
        when(scheduleRepository.save(any(AdventureSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookingRepository.save(any(AdventureCheckoutBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdventureBookingActionResponseDTO response = service.cancelBooking(userId, 104L);

        assertEquals("CANCELLED", response.getStatus());
        assertEquals(6, schedule.getAvailableSlots());
    }

    @Test
    void cancelBooking_throwsWhenRuleViolated() {
        UUID userId = UUID.randomUUID();
        AdventureCheckoutBooking booking = confirmedBooking(105L, 2, 2, 4);
        when(bookingRepository.findByIdAndUserIdForUpdateWithDetails(105L, userId)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class, () -> service.cancelBooking(userId, 105L));
    }

    private AdventureCheckoutBooking confirmedBooking(Long id, int participants, int hoursFromNow, int slots) {
        Adventure adventure = new Adventure();
        adventure.setId(11L);
        adventure.setName("Island Safari");
        adventure.setBasePrice(BigDecimal.valueOf(100));

        AdventureSchedule schedule = schedule(77L, adventure, slots, hoursFromNow);

        AdventureCheckoutBooking booking = new AdventureCheckoutBooking();
        booking.setId(id);
        booking.setAdventure(adventure);
        booking.setSchedule(schedule);
        booking.setParticipants(participants);
        booking.setTotalPrice(BigDecimal.valueOf(100L * participants));
        booking.setStatus(AdventureCheckoutStatus.CONFIRMED);
        booking.setWhatToBring("Sunscreen,Hat");
        return booking;
    }

    private AdventureSchedule schedule(Long id, Adventure adventure, int slots, int hoursFromNow) {
        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setId(id);
        schedule.setAdventure(adventure);
        schedule.setAvailableSlots(slots);
        schedule.setStatus("AVAILABLE");
        schedule.setStartDate(LocalDateTime.now().plusHours(hoursFromNow));
        schedule.setEndDate(LocalDateTime.now().plusHours(hoursFromNow + 2));
        return schedule;
    }
}
