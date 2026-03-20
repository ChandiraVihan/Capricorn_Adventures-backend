package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.RoomResponse;

import java.time.LocalDate;
import java.util.List;

public interface RoomSearchService {

    List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests);
}
