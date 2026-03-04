package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.RoomDetailsDTO;

import java.time.LocalDate;

public interface RoomService {
    RoomDetailsDTO getRoomDetails(Long roomId);
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
    RoomDetailsDTO getRoomDetailsWithAvailability(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);
}
