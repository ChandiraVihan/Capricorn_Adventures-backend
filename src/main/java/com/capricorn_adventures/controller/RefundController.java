package com.capricorn_adventures.controller;

import com.capricorn_adventures.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/room/{id}")
    public ResponseEntity<Map<String, String>> refundRoomBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String reason = request.getOrDefault("reason", "User requested refund");
        refundService.processRoomRefund(id, reason);
        
        return ResponseEntity.ok(Map.of("message", "Room refund processed successfully"));
    }

    @PostMapping("/adventure/{id}")
    public ResponseEntity<Map<String, String>> refundAdventureBooking(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String reason = request.getOrDefault("reason", "User requested refund");
        refundService.processAdventureRefund(id, reason);
        
        return ResponseEntity.ok(Map.of("message", "Adventure refund processed successfully"));
    }
}
