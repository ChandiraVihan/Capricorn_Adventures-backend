package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.RoomServiceDailyOpsSummaryResponseDTO;
import com.capricorn_adventures.dto.RoomServiceDashboardResponseDTO;
import com.capricorn_adventures.dto.RoomServiceOrderAssignmentRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCardDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCreateRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderStatusUpdateRequestDTO;
import com.capricorn_adventures.service.RoomServiceOrderService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/room-service/orders")
@CrossOrigin(origins = "*")
public class RoomServiceOrderController {

    private final RoomServiceOrderService orderService;

    public RoomServiceOrderController(RoomServiceOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<RoomServiceOrderCardDTO> createOrder(@Valid @RequestBody RoomServiceOrderCreateRequestDTO request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PatchMapping("/{orderId}/assign")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<RoomServiceOrderCardDTO> assignStaff(@PathVariable Long orderId,
                                                               @Valid @RequestBody RoomServiceOrderAssignmentRequestDTO request) {
        return ResponseEntity.ok(orderService.assignStaff(orderId, request));
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('STAFF')")
    public ResponseEntity<RoomServiceOrderCardDTO> updateStatus(@PathVariable Long orderId,
                                                                @Valid @RequestBody RoomServiceOrderStatusUpdateRequestDTO request) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, request));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<RoomServiceDashboardResponseDTO> getDashboard(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Integer minRoom,
            @RequestParam(required = false) Integer maxRoom) {
        return ResponseEntity.ok(orderService.getDashboard(floor, minRoom, maxRoom));
    }

    @GetMapping(value = "/dashboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public SseEmitter subscribeDashboard() {
        return orderService.subscribeDashboardEvents();
    }

    @GetMapping("/daily-summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<RoomServiceDailyOpsSummaryResponseDTO> getDailySummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(orderService.getDailySummary(date));
    }
}
