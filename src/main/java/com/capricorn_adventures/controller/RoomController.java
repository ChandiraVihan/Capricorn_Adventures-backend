package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.RoomDetailsDTO;
import com.capricorn_adventures.dto.RoomResponse;
import com.capricorn_adventures.dto.RoomSearchRequest;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.service.RoomSearchService;
import com.capricorn_adventures.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin(origins = "*")
public class RoomController {

    private final RoomService roomService;
    private final RoomSearchService roomSearchService;

    @Autowired
    public RoomController(RoomService roomService, RoomSearchService roomSearchService) {
        this.roomService = roomService;
        this.roomSearchService = roomSearchService;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailsDTO> getRoomDetails(@PathVariable Long roomId) {
        if (roomId < 0) {
            throw new BadRequestException("Room ID cannot be negative: " + roomId);
        }
        RoomDetailsDTO roomDetails = roomService.getRoomDetails(roomId);
        return ResponseEntity.ok(roomDetails);
    }

    @GetMapping("/{roomId}/availability")
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {

        boolean isAvailable = roomService.isRoomAvailable(roomId, checkInDate, checkOutDate);

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", roomId);
        response.put("isAvailable", isAvailable);
        response.put("checkInDate", checkInDate);
        response.put("checkOutDate", checkOutDate);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RoomResponse>> searchRooms(
            @Valid @ModelAttribute RoomSearchRequest request) {

        List<RoomResponse> rooms = roomSearchService.searchAvailableRooms(
                request.getCheckIn(), request.getCheckOut(), request.getGuests());
        return ResponseEntity.ok(rooms);
    }
}
