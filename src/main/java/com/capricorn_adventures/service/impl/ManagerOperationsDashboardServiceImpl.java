package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.entity.OperationsAlert;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.repository.OperationsAlertRepository;
import com.capricorn_adventures.service.ManagerOperationsDashboardService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ManagerOperationsDashboardServiceImpl implements ManagerOperationsDashboardService {

    private static final int REFRESH_INTERVAL_SECONDS = 60;

    private final AdventureScheduleRepository scheduleRepository;
    private final OperationsAlertRepository operationsAlertRepository;

    public ManagerOperationsDashboardServiceImpl(AdventureScheduleRepository scheduleRepository,
                                                 OperationsAlertRepository operationsAlertRepository) {
        this.scheduleRepository = scheduleRepository;
        this.operationsAlertRepository = operationsAlertRepository;
    }

    @Override
    public ManagerOperationsDashboardResponseDTO getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        List<AdventureSchedule> todaySchedules = scheduleRepository.findDashboardSchedulesBetween(todayStart, tomorrowStart);
        List<Long> todayScheduleIds = todaySchedules.stream().map(AdventureSchedule::getId).toList();
        Map<Long, List<ManagerOperationsDashboardResponseDTO.IssueDTO>> issuesByScheduleId = loadIssues(todayScheduleIds);

        LocalDate weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStartDateTime = weekStart.atStartOfDay();
        LocalDateTime nextWeekStartDateTime = weekStart.plusDays(7).atStartOfDay();
        List<AdventureSchedule> weeklySchedules = scheduleRepository.findDashboardSchedulesBetween(weekStartDateTime, nextWeekStartDateTime);

        ManagerOperationsDashboardResponseDTO response = new ManagerOperationsDashboardResponseDTO();
        response.setBusinessDate(today);
        response.setGeneratedAt(now);
        response.setAutoRefreshEnabled(true);
        response.setRefreshIntervalSeconds(REFRESH_INTERVAL_SECONDS);
        response.setTodayTours(todaySchedules.stream()
                .map(schedule -> mapTour(schedule, now, issuesByScheduleId.getOrDefault(schedule.getId(), List.of())))
                .collect(Collectors.toList()));
        response.setWeeklyOccupancy(buildWeeklyOccupancy(weekStart, weeklySchedules));
        return response;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public ManagerOperationsDashboardResponseDTO.TourSlotDTO assignGuide(Long scheduleId, String guideName) {
        AdventureSchedule schedule = scheduleRepository.findByIdWithAdventure(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + scheduleId));

        schedule.setAssignedGuideName(guideName == null ? null : guideName.trim());
        AdventureSchedule updated = scheduleRepository.save(schedule);

        Map<Long, List<ManagerOperationsDashboardResponseDTO.IssueDTO>> issuesByScheduleId = loadIssues(List.of(updated.getId()));
        return mapTour(updated, LocalDateTime.now(), issuesByScheduleId.getOrDefault(updated.getId(), List.of()));
    }

    private Map<Long, List<ManagerOperationsDashboardResponseDTO.IssueDTO>> loadIssues(List<Long> scheduleIds) {
        if (scheduleIds.isEmpty()) {
            return Map.of();
        }

        return operationsAlertRepository.findActiveAlertsForSchedules(scheduleIds).stream()
                .map(this::mapIssue)
                .collect(Collectors.groupingBy(ManagerOperationsDashboardResponseDTO.IssueDTO::getScheduleId));
    }

    private ManagerOperationsDashboardResponseDTO.TourSlotDTO mapTour(AdventureSchedule schedule,
                                                                       LocalDateTime now,
                                                                       List<ManagerOperationsDashboardResponseDTO.IssueDTO> issues) {
        ManagerOperationsDashboardResponseDTO.TourSlotDTO dto = new ManagerOperationsDashboardResponseDTO.TourSlotDTO();
        dto.setScheduleId(schedule.getId());
        dto.setAdventureId(schedule.getAdventure().getId());
        dto.setAdventureName(schedule.getAdventure().getName());
        dto.setStartDateTime(schedule.getStartDate());
        dto.setEndDateTime(schedule.getEndDate());
        dto.setStatus(resolveDisplayStatus(schedule, now));
        dto.setAssignedGuideName(schedule.getAssignedGuideName());
        dto.setGuideAssigned(schedule.getAssignedGuideName() != null && !schedule.getAssignedGuideName().isBlank());
        dto.setGuideAssignmentRequired(!dto.isGuideAssigned());
        dto.setQuickActionLabel("Assign Guide");
        dto.setCheckedInCustomerCount(normalizeCount(schedule.getCheckedInCustomerCount()));
        dto.setAvailableSlots(normalizeCount(schedule.getAvailableSlots()));
        dto.setTotalCapacity(resolveTotalCapacity(schedule));
        dto.setIssues(issues);
        return dto;
    }

    private ManagerOperationsDashboardResponseDTO.IssueDTO mapIssue(OperationsAlert alert) {
        ManagerOperationsDashboardResponseDTO.IssueDTO dto = new ManagerOperationsDashboardResponseDTO.IssueDTO();
        dto.setAlertId(alert.getId());
        dto.setScheduleId(alert.getSchedule().getId());
        dto.setType(alert.getType());
        dto.setPriority(alert.getPriority());
        dto.setTitle(alert.getTitle());
        dto.setMessage(alert.getMessage());
        dto.setRaisedAt(alert.getCreatedAt());
        return dto;
    }

    private List<ManagerOperationsDashboardResponseDTO.OccupancyDTO> buildWeeklyOccupancy(LocalDate weekStart,
                                                                                           List<AdventureSchedule> weeklySchedules) {
        Map<LocalDate, List<AdventureSchedule>> schedulesByDate = weeklySchedules.stream()
                .filter(schedule -> schedule.getStartDate() != null)
                .collect(Collectors.groupingBy(schedule -> schedule.getStartDate().toLocalDate()));

        return java.util.stream.IntStream.range(0, 7)
                .mapToObj(offset -> weekStart.plusDays(offset))
                .map(date -> mapOccupancy(date, schedulesByDate.getOrDefault(date, List.of())))
                .collect(Collectors.toList());
    }

    private ManagerOperationsDashboardResponseDTO.OccupancyDTO mapOccupancy(LocalDate date, List<AdventureSchedule> schedules) {
        int totalCapacity = schedules.stream().mapToInt(this::resolveTotalCapacity).sum();
        int availableCapacity = schedules.stream().mapToInt(schedule -> normalizeCount(schedule.getAvailableSlots())).sum();
        int bookedCapacity = Math.max(0, totalCapacity - availableCapacity);

        ManagerOperationsDashboardResponseDTO.OccupancyDTO dto = new ManagerOperationsDashboardResponseDTO.OccupancyDTO();
        dto.setDate(date);
        dto.setDayLabel(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        dto.setTotalCapacity(totalCapacity);
        dto.setAvailableCapacity(availableCapacity);
        dto.setBookedCapacity(bookedCapacity);
        return dto;
    }

    private String resolveDisplayStatus(AdventureSchedule schedule, LocalDateTime now) {
        String status = schedule.getStatus();
        if (status != null && "CANCELLED".equalsIgnoreCase(status)) {
            return "Cancelled";
        }
        if (schedule.getStartDate() != null && schedule.getEndDate() != null
                && !schedule.getStartDate().isAfter(now)
                && !schedule.getEndDate().isBefore(now)) {
            return "In Progress";
        }
        if (schedule.getEndDate() != null && schedule.getEndDate().isBefore(now)) {
            return "Completed";
        }
        return "Upcoming";
    }

    private int resolveTotalCapacity(AdventureSchedule schedule) {
        Integer totalCapacity = schedule.getTotalCapacity();
        if (totalCapacity != null) {
            return Math.max(totalCapacity, 0);
        }
        Integer availableSlots = schedule.getAvailableSlots();
        return availableSlots == null ? 0 : Math.max(availableSlots, 0);
    }

    private int normalizeCount(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }
}