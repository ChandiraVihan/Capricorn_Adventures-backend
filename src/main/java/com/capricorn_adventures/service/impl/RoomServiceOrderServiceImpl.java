package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.RoomServiceDailyOpsSummaryResponseDTO;
import com.capricorn_adventures.dto.RoomServiceDashboardResponseDTO;
import com.capricorn_adventures.dto.RoomServiceOrderAssignmentRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCardDTO;
import com.capricorn_adventures.dto.RoomServiceOrderCreateRequestDTO;
import com.capricorn_adventures.dto.RoomServiceOrderEventDTO;
import com.capricorn_adventures.dto.RoomServiceOrderStatusUpdateRequestDTO;
import com.capricorn_adventures.entity.RoomServiceOrder;
import com.capricorn_adventures.entity.RoomServiceOrderStatus;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.exception.BadRequestException;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.RoomServiceOrderRepository;
import com.capricorn_adventures.repository.UserRepository;
import com.capricorn_adventures.service.NotificationService;
import com.capricorn_adventures.service.RoomServiceDashboardEventPublisher;
import com.capricorn_adventures.service.RoomServiceOrderService;
import com.capricorn_adventures.service.RoomServicePushNotificationService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class RoomServiceOrderServiceImpl implements RoomServiceOrderService {

    private static final int STALE_THRESHOLD_MINUTES = 20;

    private final RoomServiceOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RoomServicePushNotificationService pushNotificationService;
    private final RoomServiceDashboardEventPublisher eventPublisher;
    private final NotificationService notificationService;

    public RoomServiceOrderServiceImpl(RoomServiceOrderRepository orderRepository,
                                       UserRepository userRepository,
                                       RoomServicePushNotificationService pushNotificationService,
                                       RoomServiceDashboardEventPublisher eventPublisher,
                                       NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.pushNotificationService = pushNotificationService;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public RoomServiceOrderCardDTO createOrder(RoomServiceOrderCreateRequestDTO request) {
        List<String> normalizedItems = request.getItemsOrdered().stream()
                .map(item -> item == null ? "" : item.trim())
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());

        if (normalizedItems.isEmpty()) {
            throw new BadRequestException("At least one valid room service item is required");
        }

        LocalDateTime now = LocalDateTime.now();

        RoomServiceOrder order = new RoomServiceOrder();
        order.setRoomNumber(request.getRoomNumber());
        order.setFloorNumber(request.getFloorNumber());
        order.setItemsOrdered(normalizedItems);
        order.setPlacedAt(now);
        order.setStatus(RoomServiceOrderStatus.RECEIVED);
        order.setLastStatusUpdatedAt(now);
        order.setStaleFlag(false);
        order.setStaleAlertedAt(null);

        RoomServiceOrder saved = orderRepository.save(order);
        publishOrderEvent("ORDER_RECEIVED", "Room service order received", saved);
        return toCard(saved);
    }

    @Override
    @Transactional
    public RoomServiceOrderCardDTO assignStaff(Long orderId, RoomServiceOrderAssignmentRequestDTO request) {
        RoomServiceOrder order = findOrder(orderId);
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with ID: " + request.getStaffId()));

        if (staff.getRole() != User.UserRole.STAFF) {
            throw new BadRequestException("Assigned user must have STAFF role");
        }

        order.setAssignedStaff(staff);
        RoomServiceOrder saved = orderRepository.save(order);
        pushNotificationService.sendOrderAssignmentNotification(staff, saved);

        publishOrderEvent("ORDER_ASSIGNED", "Staff member assigned to room service order", saved);
        return toCard(saved);
    }

    @Override
    @Transactional
    public RoomServiceOrderCardDTO updateStatus(Long orderId, RoomServiceOrderStatusUpdateRequestDTO request) {
        RoomServiceOrder order = findOrder(orderId);
        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        order.setLastStatusUpdatedAt(LocalDateTime.now());
        order.setStaleFlag(false);
        order.setStaleAlertedAt(null);

        RoomServiceOrder saved = orderRepository.save(order);
        publishOrderEvent("ORDER_STATUS_UPDATED", "Room service order status updated", saved);
        return toCard(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomServiceDashboardResponseDTO getDashboard(Integer floor, Integer minRoom, Integer maxRoom) {
        validateRoomRange(minRoom, maxRoom);

        List<RoomServiceOrderStatus> activeStatuses = List.of(RoomServiceOrderStatus.RECEIVED, RoomServiceOrderStatus.PREPARING);
        List<RoomServiceOrderCardDTO> activeOrders = orderRepository
                .findActiveOrdersForDashboard(activeStatuses, floor, minRoom, maxRoom)
                .stream()
                .sorted(Comparator.comparing(RoomServiceOrder::getPlacedAt).reversed())
                .map(this::toCard)
                .toList();

        RoomServiceDashboardResponseDTO response = new RoomServiceDashboardResponseDTO();
        response.setGeneratedAt(LocalDateTime.now());
        response.setAutoRefreshEnabled(true);
        response.setStaleThresholdMinutes(STALE_THRESHOLD_MINUTES);
        response.setActiveOrders(activeOrders);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomServiceDailyOpsSummaryResponseDTO getDailySummary(LocalDate date) {
        LocalDate businessDate = date == null ? LocalDate.now() : date;
        LocalDateTime start = businessDate.atStartOfDay();
        LocalDateTime end = businessDate.plusDays(1).atStartOfDay();

        List<RoomServiceOrder> dayOrders = orderRepository.findByPlacedAtBetweenOrderByPlacedAtAsc(start, end);

        List<RoomServiceOrder> delivered = dayOrders.stream()
                .filter(order -> order.getStatus() == RoomServiceOrderStatus.DELIVERED)
                .filter(order -> order.getPlacedAt() != null && order.getLastStatusUpdatedAt() != null)
                .toList();

        long averageMinutes = delivered.isEmpty() ? 0 : Math.round(
                delivered.stream()
                        .mapToLong(order -> Duration.between(order.getPlacedAt(), order.getLastStatusUpdatedAt()).toMinutes())
                        .average()
                        .orElse(0)
        );

        List<RoomServiceOrderCardDTO> unresolved = dayOrders.stream()
                .filter(order -> order.getStatus() != RoomServiceOrderStatus.DELIVERED)
                .map(this::toCard)
                .toList();

        RoomServiceDailyOpsSummaryResponseDTO response = new RoomServiceDailyOpsSummaryResponseDTO();
        response.setBusinessDate(businessDate);
        response.setTotalOrders(dayOrders.size());
        response.setAverageDeliveryMinutes(averageMinutes);
        response.setUnresolvedOrdersCount(unresolved.size());
        response.setUnresolvedOrders(unresolved);
        return response;
    }

    @Override
    public SseEmitter subscribeDashboardEvents() {
        return eventPublisher.subscribe();
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void scanAndAlertStaleOrders() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(STALE_THRESHOLD_MINUTES);
        List<RoomServiceOrderStatus> activeStatuses = List.of(RoomServiceOrderStatus.RECEIVED, RoomServiceOrderStatus.PREPARING);
        List<RoomServiceOrder> staleOrders = orderRepository.findUnalertedStaleOrders(activeStatuses, cutoff);

        if (staleOrders.isEmpty()) {
            return;
        }

        List<String> managerEmails = userRepository
                .findByRoleAndStatus(User.UserRole.MANAGER, User.UserStatus.ACTIVE)
                .stream()
                .map(User::getEmail)
                .filter(email -> email != null && !email.isBlank())
                .toList();

        for (RoomServiceOrder order : staleOrders) {
            order.setStaleFlag(true);
            order.setStaleAlertedAt(LocalDateTime.now());
            RoomServiceOrder saved = orderRepository.save(order);

            notificationService.sendRoomServiceStaleOrderAlert(managerEmails, saved);
            publishOrderEvent("ORDER_STALE_ALERT", "Room service order has not been updated for over 20 minutes", saved);
        }
    }

    private RoomServiceOrder findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Room service order not found with ID: " + orderId));
    }

    private void validateStatusTransition(RoomServiceOrderStatus current, RoomServiceOrderStatus next) {
        if (current == next) {
            return;
        }

        if (current == RoomServiceOrderStatus.RECEIVED && next == RoomServiceOrderStatus.PREPARING) {
            return;
        }

        if (current == RoomServiceOrderStatus.PREPARING && next == RoomServiceOrderStatus.DELIVERED) {
            return;
        }

        throw new BadRequestException("Invalid status transition from " + current + " to " + next);
    }

    private void validateRoomRange(Integer minRoom, Integer maxRoom) {
        if (minRoom != null && minRoom < 1) {
            throw new BadRequestException("minRoom must be at least 1");
        }
        if (maxRoom != null && maxRoom < 1) {
            throw new BadRequestException("maxRoom must be at least 1");
        }
        if (minRoom != null && maxRoom != null && minRoom > maxRoom) {
            throw new BadRequestException("minRoom cannot be greater than maxRoom");
        }
    }

    private RoomServiceOrderCardDTO toCard(RoomServiceOrder order) {
        RoomServiceOrderCardDTO dto = new RoomServiceOrderCardDTO();
        dto.setOrderId(order.getId());
        dto.setRoomNumber(order.getRoomNumber());
        dto.setFloorNumber(order.getFloorNumber());
        dto.setItemsOrdered(order.getItemsOrdered());
        dto.setPlacedAt(order.getPlacedAt());
        dto.setStatus(order.getStatus());
        dto.setStaleFlag(order.isStaleFlag());
        dto.setLastStatusUpdatedAt(order.getLastStatusUpdatedAt());

        if (order.getAssignedStaff() != null) {
            dto.setAssignedStaffId(order.getAssignedStaff().getId());
            String firstName = order.getAssignedStaff().getFirstName();
            String lastName = order.getAssignedStaff().getLastName();
            String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
            dto.setAssignedStaffName(fullName.isEmpty() ? order.getAssignedStaff().getEmail() : fullName);
        }

        return dto;
    }

    private void publishOrderEvent(String eventType, String message, RoomServiceOrder order) {
        RoomServiceOrderEventDTO event = new RoomServiceOrderEventDTO();
        event.setEventType(eventType);
        event.setMessage(message);
        event.setOccurredAt(LocalDateTime.now());
        event.setOrder(toCard(order));
        eventPublisher.publish(event);
    }
}
