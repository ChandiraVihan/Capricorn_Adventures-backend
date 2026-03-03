package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.RoomDetailsDTO;
import com.capricorn_adventures.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*") 
public class RoomController {

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<RoomDetailsDTO> getRoomDetails(@PathVariable Long roomId) {
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
}
