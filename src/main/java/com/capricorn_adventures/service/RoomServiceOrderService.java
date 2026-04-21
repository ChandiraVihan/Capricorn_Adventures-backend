package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.RoomServiceDailyOpsSummaryResponseDTO;
import com.capricorn_adventures.dto.RoomServiceDashboardResponseDTO;
import com.capricorn_adventures.dto.RoomServiceOrderAssignmentRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCardDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCreateRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderStatusUpdateRequestDTO;
import java.time.LocalDate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface RoomServiceOrderService {

    RoomServiceOrderCardDTO createOrder(RoomServiceOrderCreateRequestDTO request);

    RoomServiceOrderCardDTO assignStaff(Long orderId, RoomServiceOrderAssignmentRequestDTO request);

    RoomServiceOrderCardDTO updateStatus(Long orderId, RoomServiceOrderStatusUpdateRequestDTO request);

    RoomServiceDashboardResponseDTO getDashboard(Integer floor, Integer minRoom, Integer maxRoom);

    RoomServiceDailyOpsSummaryResponseDTO getDailySummary(LocalDate date);

    SseEmitter subscribeDashboardEvents();

    void scanAndAlertStaleOrders();
}
