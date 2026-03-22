package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.RoomDetailsDTO;
import com.capricorn_adventures.dto.RoomResponse;
import com.capricorn_adventures.service.RoomSearchService;
import com.capricorn_adventures.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomSearchServiceImpl implements RoomSearchService {

    private final RoomService roomService;

    public RoomSearchServiceImpl(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    public List<RoomResponse> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        List<RoomDetailsDTO> availableRooms = roomService.searchRooms(checkIn, checkOut, guests);

        return availableRooms.stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    private RoomResponse mapToRoomResponse(RoomDetailsDTO dto) {
        return new RoomResponse(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getBasePrice(),
                dto.getImages(),
                dto.getAmenities()
        );
    }
}
