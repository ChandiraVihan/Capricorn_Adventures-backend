package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.AdventureCheckoutConfirmResponseDTO;
import com.capricorn_adventures.dto.AdventureCheckoutStartRequestDTO;
import com.capricorn_adventures.dto.AdventureCheckoutSummaryResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdventureCheckoutServiceImplTest {

    @Mock
    private AdventureRepository adventureRepository;

    @Mock
    private AdventureScheduleRepository adventureScheduleRepository;

    @Mock
    private AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;

    @Mock
    private UserRepository userRepository;

    private AdventureCheckoutServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdventureCheckoutServiceImpl(
                adventureRepository,
                adventureScheduleRepository,
                adventureCheckoutBookingRepository,
                userRepository
        );
    }

    @Test
    void startCheckout_returnsSummaryWithDateTimeParticipantsAndTotalPrice() {
        Adventure adventure = adventure(11L, "Whale Watching", BigDecimal.valueOf(80));
        AdventureSchedule schedule = schedule(21L, adventure, 6);

        AdventureCheckoutStartRequestDTO request = new AdventureCheckoutStartRequestDTO();
        request.setAdventureId(11L);
        request.setScheduleId(21L);
        request.setParticipants(3);

        when(adventureRepository.findByIdWithDetails(11L)).thenReturn(Optional.of(adventure));
        when(adventureScheduleRepository.findByIdWithAdventure(21L)).thenReturn(Optional.of(schedule));
        when(adventureCheckoutBookingRepository.save(any(AdventureCheckoutBooking.class)))
                .thenAnswer(invocation -> {
                    AdventureCheckoutBooking booking = invocation.getArgument(0);
                    booking.setId(99L);
                    return booking;
                });

        AdventureCheckoutSummaryResponseDTO response = service.startCheckout(request, null);

        assertEquals(99L, response.getCheckoutId());
        assertEquals(11L, response.getAdventureId());
        assertEquals("Whale Watching", response.getAdventureName());
        assertEquals(3, response.getParticipants());
        assertEquals(BigDecimal.valueOf(240), response.getTotalPrice());
        assertTrue(response.isCanProceedAsGuest());
        assertTrue(response.isCanLoginOrRegister());
        assertTrue(response.isSelectionRetained());
    }

    @Test
    void confirmCheckout_paymentFails_keepsBookingUnconfirmedAndAllowsRetry() {
        Adventure adventure = adventure(11L, "Kayak", BigDecimal.valueOf(50));
        AdventureSchedule schedule = schedule(21L, adventure, 6);
        AdventureCheckoutBooking booking = pendingBooking(31L, adventure, schedule, 2);

        when(adventureCheckoutBookingRepository.findByIdWithDetails(31L)).thenReturn(Optional.of(booking));
        when(adventureCheckoutBookingRepository.save(any(AdventureCheckoutBooking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdventureCheckoutConfirmResponseDTO response = service.confirmCheckout(31L, false);

        assertFalse(response.isConfirmed());
        assertTrue(response.isRetryAllowed());
        assertEquals("RETRY_OR_CHANGE_PAYMENT", response.getNextAction());
        assertEquals(AdventureCheckoutStatus.PAYMENT_FAILED, booking.getStatus());
    }

    @Test
    void confirmCheckout_whenCapacityDrops_bloacksConfirmationWithSlotMessage() {
        Adventure adventure = adventure(11L, "Kayak", BigDecimal.valueOf(50));
        AdventureSchedule staleSchedule = schedule(21L, adventure, 6);
        AdventureCheckoutBooking booking = pendingBooking(31L, adventure, staleSchedule, 4);

        AdventureSchedule lockedSchedule = schedule(21L, adventure, 2);

        when(adventureCheckoutBookingRepository.findByIdWithDetails(31L)).thenReturn(Optional.of(booking));
        when(adventureScheduleRepository.findByIdForUpdateWithAdventure(21L)).thenReturn(Optional.of(lockedSchedule));

        assertThrows(BadRequestException.class, () -> service.confirmCheckout(31L, true));
    }

    @Test
    void confirmCheckout_whenPaymentSucceeds_returnsReferenceAndInstructions() {
        Adventure adventure = adventure(11L, "Kayak", BigDecimal.valueOf(50));
        adventure.setLocation("Jetty A");
        adventure.setInclusions("Water bottle,Sun hat");

        AdventureSchedule staleSchedule = schedule(21L, adventure, 6);
        AdventureCheckoutBooking booking = pendingBooking(31L, adventure, staleSchedule, 2);

        AdventureSchedule lockedSchedule = schedule(21L, adventure, 5);

        when(adventureCheckoutBookingRepository.findByIdWithDetails(31L)).thenReturn(Optional.of(booking));
        when(adventureScheduleRepository.findByIdForUpdateWithAdventure(21L)).thenReturn(Optional.of(lockedSchedule));
        when(adventureScheduleRepository.save(any(AdventureSchedule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(adventureCheckoutBookingRepository.save(any(AdventureCheckoutBooking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdventureCheckoutConfirmResponseDTO response = service.confirmCheckout(31L, true);

        assertTrue(response.isConfirmed());
        assertTrue(response.getBookingReference().startsWith("CAP-ADV-"));
        assertEquals("Jetty A", response.getMeetingPoint());
        assertEquals(2, response.getWhatToBring().size());
        assertEquals(3, lockedSchedule.getAvailableSlots());
        assertEquals(AdventureCheckoutStatus.CONFIRMED, booking.getStatus());

        ArgumentCaptor<AdventureSchedule> scheduleCaptor = ArgumentCaptor.forClass(AdventureSchedule.class);
        verify(adventureScheduleRepository).save(scheduleCaptor.capture());
        assertEquals(3, scheduleCaptor.getValue().getAvailableSlots());
    }

    private Adventure adventure(Long id, String name, BigDecimal price) {
        Adventure adventure = new Adventure();
        adventure.setId(id);
        adventure.setName(name);
        adventure.setBasePrice(price);
        adventure.setActive(true);
        return adventure;
    }

    private AdventureSchedule schedule(Long id, Adventure adventure, int slots) {
        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setId(id);
        schedule.setAdventure(adventure);
        schedule.setAvailableSlots(slots);
        schedule.setStatus("AVAILABLE");
        schedule.setStartDate(LocalDateTime.now().plusDays(2));
        schedule.setEndDate(LocalDateTime.now().plusDays(2).plusHours(3));
        return schedule;
    }

    private AdventureCheckoutBooking pendingBooking(Long id, Adventure adventure, AdventureSchedule schedule, int participants) {
        AdventureCheckoutBooking booking = new AdventureCheckoutBooking();
        booking.setId(id);
        booking.setAdventure(adventure);
        booking.setSchedule(schedule);
        booking.setParticipants(participants);
        booking.setUnitPrice(adventure.getBasePrice());
        booking.setTotalPrice(adventure.getBasePrice().multiply(BigDecimal.valueOf(participants)));
        booking.setStatus(AdventureCheckoutStatus.PENDING);
        return booking;
    }
}
