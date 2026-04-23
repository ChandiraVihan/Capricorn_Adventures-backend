package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.RoomServiceDailyOpsSummaryResponseDTO;
import com.capricorn_adventures.dto.RoomServiceOrderAssignmentRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCardDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCreateRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderStatusUpdateRequestDTO;
import com.capricorn_adventures.entity.RoomServiceOrder;
import com.capricorn_adventures.entity.RoomServiceOrderStatus;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.repository.RoomServiceOrderRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.NotificationService;
import com.capricorn_adventures.service.RoomServiceDashboardEventPublisher;
import com.capricorn_adventures.service.RoomServicePushNotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceOrderServiceImplTest {

    @Mock
    private RoomServiceOrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomServicePushNotificationService pushNotificationService;

    @Mock
    private RoomServiceDashboardEventPublisher eventPublisher;

    @Mock
    private NotificationService notificationService;

    private RoomServiceOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RoomServiceOrderServiceImpl(
                orderRepository,
                userRepository,
                pushNotificationService,
                eventPublisher,
                notificationService
        );
    }

    @Test
    void createOrder_savesReceivedOrderAndPublishesEvent() {
        RoomServiceOrderCreateRequestDTO request = new RoomServiceOrderCreateRequestDTO();
        request.setRoomNumber(305);
        request.setFloorNumber(3);
        request.setItemsOrdered(List.of("Chicken burger", "Lime soda"));

        when(orderRepository.save(any(RoomServiceOrder.class))).thenAnswer(invocation -> {
            RoomServiceOrder order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        RoomServiceOrderCardDTO response = service.createOrder(request);

        assertEquals(10L, response.getOrderId());
        assertEquals(305, response.getRoomNumber());
        assertEquals(RoomServiceOrderStatus.RECEIVED, response.getStatus());
        verify(eventPublisher).publish(any());
    }

    @Test
    void assignStaff_notifiesAssignedStaffAndPublishesEvent() {
        UUID staffId = UUID.randomUUID();

        RoomServiceOrder order = new RoomServiceOrder();
        order.setId(21L);
        order.setRoomNumber(204);
        order.setFloorNumber(2);
        order.setItemsOrdered(List.of("Club sandwich"));
        order.setPlacedAt(LocalDateTime.now().minusMinutes(2));
        order.setStatus(RoomServiceOrderStatus.RECEIVED);
        order.setLastStatusUpdatedAt(LocalDateTime.now().minusMinutes(2));

        User staff = User.builder()
                .id(staffId)
                .email("staff@capricorn.com")
                .firstName("Nimal")
                .lastName("Silva")
                .role(User.UserRole.STAFF)
                .status(User.UserStatus.ACTIVE)
                .build();

        RoomServiceOrderAssignmentRequestDTO request = new RoomServiceOrderAssignmentRequestDTO();
        request.setStaffId(staffId);

        when(orderRepository.findById(21L)).thenReturn(Optional.of(order));
        when(userRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(orderRepository.save(any(RoomServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomServiceOrderCardDTO response = service.assignStaff(21L, request);

        assertEquals(staffId, response.getAssignedStaffId());
        assertEquals("Nimal Silva", response.getAssignedStaffName());
        verify(pushNotificationService).sendOrderAssignmentNotification(staff, order);
        verify(eventPublisher).publish(any());
    }

    @Test
    void updateStatus_changesToDeliveredAndReflectsInDailySummary() {
        RoomServiceOrder order = new RoomServiceOrder();
        order.setId(50L);
        order.setRoomNumber(118);
        order.setFloorNumber(1);
        order.setItemsOrdered(List.of("Iced tea"));
        order.setStatus(RoomServiceOrderStatus.PREPARING);
        order.setPlacedAt(LocalDateTime.now().minusMinutes(30));
        order.setLastStatusUpdatedAt(LocalDateTime.now().minusMinutes(25));

        RoomServiceOrderStatusUpdateRequestDTO updateRequest = new RoomServiceOrderStatusUpdateRequestDTO();
        updateRequest.setStatus(RoomServiceOrderStatus.DELIVERED);

        when(orderRepository.findById(50L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(RoomServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RoomServiceOrderCardDTO updated = service.updateStatus(50L, updateRequest);
        assertEquals(RoomServiceOrderStatus.DELIVERED, updated.getStatus());
        assertFalse(updated.isStaleFlag());

        LocalDate today = LocalDate.now();
        when(orderRepository.findByPlacedAtBetweenOrderByPlacedAtAsc(today.atStartOfDay(), today.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(order));

        RoomServiceDailyOpsSummaryResponseDTO summary = service.getDailySummary(today);
        assertEquals(1, summary.getTotalOrders());
        assertEquals(0, summary.getUnresolvedOrdersCount());
        assertTrue(summary.getAverageDeliveryMinutes() >= 0);
    }

    @Test
    void scanAndAlertStaleOrders_flagsAndNotifiesManagers() {
        RoomServiceOrder staleOrder = new RoomServiceOrder();
        staleOrder.setId(99L);
        staleOrder.setRoomNumber(412);
        staleOrder.setFloorNumber(4);
        staleOrder.setItemsOrdered(List.of("Pasta"));
        staleOrder.setStatus(RoomServiceOrderStatus.RECEIVED);
        staleOrder.setPlacedAt(LocalDateTime.now().minusMinutes(40));
        staleOrder.setLastStatusUpdatedAt(LocalDateTime.now().minusMinutes(30));

        User manager = User.builder()
                .id(UUID.randomUUID())
                .email("manager@capricorn.com")
                .role(User.UserRole.MANAGER)
                .status(User.UserStatus.ACTIVE)
                .build();

        when(orderRepository.findUnalertedStaleOrders(any(), any())).thenReturn(List.of(staleOrder));
        when(orderRepository.save(any(RoomServiceOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndStatus(User.UserRole.MANAGER, User.UserStatus.ACTIVE)).thenReturn(List.of(manager));

        service.scanAndAlertStaleOrders();

        assertTrue(staleOrder.isStaleFlag());
        verify(notificationService).sendRoomServiceStaleOrderAlert(List.of("manager@capricorn.com"), staleOrder);
        verify(eventPublisher).publish(any());
    }

    @Test
    void scanAndAlertStaleOrders_noOrdersDoesNothing() {
        when(orderRepository.findUnalertedStaleOrders(any(), any())).thenReturn(List.of());

        service.scanAndAlertStaleOrders();

        verify(userRepository, never()).findByRoleAndStatus(any(), any());
        verify(notificationService, never()).sendRoomServiceStaleOrderAlert(any(), any());
    }

    @Test
    void createOrder_rejectsEmptyNormalizedItems() {
        RoomServiceOrderCreateRequestDTO request = new RoomServiceOrderCreateRequestDTO();
        request.setRoomNumber(102);
        request.setFloorNumber(1);
        request.setItemsOrdered(List.of("   ", ""));

        assertThrows(BadRequestException.class, () -> service.createOrder(request));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignStaff_rejectsNonStaffUserRole() {
        UUID managerId = UUID.randomUUID();

        RoomServiceOrder order = new RoomServiceOrder();
        order.setId(34L);
        order.setRoomNumber(210);
        order.setFloorNumber(2);
        order.setItemsOrdered(List.of("Soup"));
        order.setStatus(RoomServiceOrderStatus.RECEIVED);
        order.setPlacedAt(LocalDateTime.now().minusMinutes(5));
        order.setLastStatusUpdatedAt(LocalDateTime.now().minusMinutes(5));

        User manager = User.builder()
                .id(managerId)
                .email("manager@capricorn.com")
                .role(User.UserRole.MANAGER)
                .status(User.UserStatus.ACTIVE)
                .build();

        RoomServiceOrderAssignmentRequestDTO request = new RoomServiceOrderAssignmentRequestDTO();
        request.setStaffId(managerId);

        when(orderRepository.findById(34L)).thenReturn(Optional.of(order));
        when(userRepository.findById(managerId)).thenReturn(Optional.of(manager));

        assertThrows(BadRequestException.class, () -> service.assignStaff(34L, request));
        verify(pushNotificationService, never()).sendOrderAssignmentNotification(any(), any());
    }

    @Test
    void updateStatus_rejectsInvalidTransitionFromReceivedToDelivered() {
        RoomServiceOrder order = new RoomServiceOrder();
        order.setId(78L);
        order.setStatus(RoomServiceOrderStatus.RECEIVED);
        order.setPlacedAt(LocalDateTime.now().minusMinutes(8));
        order.setLastStatusUpdatedAt(LocalDateTime.now().minusMinutes(8));

        RoomServiceOrderStatusUpdateRequestDTO request = new RoomServiceOrderStatusUpdateRequestDTO();
        request.setStatus(RoomServiceOrderStatus.DELIVERED);

        when(orderRepository.findById(78L)).thenReturn(Optional.of(order));

        assertThrows(BadRequestException.class, () -> service.updateStatus(78L, request));
        verify(orderRepository, never()).save(any());
    }
}
