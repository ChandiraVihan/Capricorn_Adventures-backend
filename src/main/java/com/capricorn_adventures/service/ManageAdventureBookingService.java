package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.AdventureBookingActionResponseDTO;
import com.capricorn_adventures.dto.AdventureBookingDetailsDTO;
import com.capricorn_adventures.dto.AdventureRescheduleOptionsResponseDTO;
import java.util.UUID;

public interface ManageAdventureBookingService {
    AdventureBookingDetailsDTO getAdventureBookingDetails(UUID userId, Long bookingId);

    AdventureRescheduleOptionsResponseDTO getRescheduleOptions(UUID userId, Long bookingId);

    AdventureBookingActionResponseDTO rescheduleBooking(UUID userId, Long bookingId, Long newScheduleId);

    AdventureBookingActionResponseDTO cancelBooking(UUID userId, Long bookingId);
}
