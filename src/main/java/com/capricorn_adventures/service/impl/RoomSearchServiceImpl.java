package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.RoomResponse;
import com.capricorn_adventures.service.RoomSearchService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RoomSearchServiceImpl implements RoomSearchService {

    @Override
    public List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        // Simulate "No Availability" when guests is exactly 5
        if (guests == 5) {
            return Collections.emptyList();
        }

        // Return hardcoded mock data
        List<RoomResponse> rooms = new ArrayList<>();

        rooms.add(new RoomResponse(
                "Deluxe Suite",
                "Spacious suite with king-size bed, ocean view balcony, and private jacuzzi.",
                new BigDecimal("350.00")
        ));

        rooms.add(new RoomResponse(
                "Standard Room",
                "Comfortable room with queen-size bed, city view, and complimentary Wi-Fi.",
                new BigDecimal("150.00")
        ));

        rooms.add(new RoomResponse(
                "Family Villa",
                "Two-bedroom villa with living area, kitchenette, and garden access. Ideal for families.",
                new BigDecimal("500.00")
        ));

        return rooms;
    }
}
