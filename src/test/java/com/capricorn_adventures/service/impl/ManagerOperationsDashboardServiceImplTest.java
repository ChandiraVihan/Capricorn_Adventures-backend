package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureSchedule;
import com.capricorn_adventures.entity.OperationsAlert;
import com.capricorn_adventures.repository.AdventureScheduleRepository;
import com.capricorn_adventures.repository.OperationsAlertRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagerOperationsDashboardServiceImplTest {

    @Mock
    private AdventureScheduleRepository scheduleRepository;

    @Mock
    private OperationsAlertRepository operationsAlertRepository;

    private ManagerOperationsDashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ManagerOperationsDashboardServiceImpl(scheduleRepository, operationsAlertRepository);
    }

    @Test
    void getDashboard_mapsTodayToursAlertsAndWeeklyOccupancy() {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Adventure adventure = adventure(11L, "Lagoon Cruise");
        AdventureSchedule inProgress = schedule(101L, adventure, now.minusMinutes(30), now.plusMinutes(30), 2, 8, "Ananda Silva", 6, "AVAILABLE");
        AdventureSchedule guideMissing = schedule(102L, adventure, now.plusHours(2), now.plusHours(4), 0, 10, null, 4, "AVAILABLE");
        AdventureSchedule upcoming = schedule(103L, adventure, now.plusDays(2), now.plusDays(2).plusHours(3), 5, 10, "Nimali Perera", 1, "AVAILABLE");

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDate weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        when(scheduleRepository.findDashboardSchedulesBetween(todayStart, tomorrowStart))
                .thenReturn(List.of(inProgress, guideMissing));
        when(scheduleRepository.findDashboardSchedulesBetween(weekStart.atStartOfDay(), weekStart.plusDays(7).atStartOfDay()))
                .thenReturn(List.of(inProgress, guideMissing, upcoming));

        OperationsAlert alert = new OperationsAlert();
        alert.setId(501L);
        alert.setSchedule(guideMissing);
        alert.setType("COMPLAINT");
        alert.setPriority("HIGH");
        alert.setTitle("Guest complaint");
        alert.setMessage("Late pickup complaint");
        alert.setResolved(false);

        when(operationsAlertRepository.findActiveAlertsForSchedules(List.of(101L, 102L))).thenReturn(List.of(alert));

        ManagerOperationsDashboardResponseDTO response = service.getDashboard();

        assertTrue(response.isAutoRefreshEnabled());
        assertEquals(60, response.getRefreshIntervalSeconds());
        assertEquals(2, response.getTodayTours().size());
        assertEquals("In Progress", response.getTodayTours().get(0).getStatus());
        assertTrue(response.getTodayTours().get(0).isGuideAssigned());
        assertFalse(response.getTodayTours().get(1).isGuideAssigned());
        assertTrue(response.getTodayTours().get(1).isGuideAssignmentRequired());
        assertEquals("Assign Guide", response.getTodayTours().get(1).getQuickActionLabel());
        assertEquals(1, response.getTodayTours().get(1).getIssues().size());
        assertEquals("HIGH", response.getTodayTours().get(1).getIssues().get(0).getPriority());
        assertEquals(7, response.getWeeklyOccupancy().size());
        assertEquals(today, response.getBusinessDate());
    }

    @Test
    void getDashboard_marksCancelledToursAsCancelled() {
        LocalDate today = LocalDate.now();
        Adventure adventure = adventure(12L, "Sunset Paddle");
        AdventureSchedule cancelled = schedule(201L, adventure, LocalDateTime.now().plusHours(1), LocalDateTime.now().plusHours(3), 0, 10, null, 0, "CANCELLED");

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDate weekStart = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        when(scheduleRepository.findDashboardSchedulesBetween(todayStart, tomorrowStart))
                .thenReturn(List.of(cancelled));
        when(scheduleRepository.findDashboardSchedulesBetween(weekStart.atStartOfDay(), weekStart.plusDays(7).atStartOfDay()))
                .thenReturn(List.of(cancelled));
        when(operationsAlertRepository.findActiveAlertsForSchedules(List.of(201L))).thenReturn(List.of());

        ManagerOperationsDashboardResponseDTO response = service.getDashboard();

        assertEquals("Cancelled", response.getTodayTours().get(0).getStatus());
    }

    private Adventure adventure(Long id, String name) {
        Adventure adventure = new Adventure();
        adventure.setId(id);
        adventure.setName(name);
        return adventure;
    }

    private AdventureSchedule schedule(Long id,
                                       Adventure adventure,
                                       LocalDateTime start,
                                       LocalDateTime end,
                                       int availableSlots,
                                       int totalCapacity,
                                       String guideName,
                                       int checkedIn,
                                       String status) {
        AdventureSchedule schedule = new AdventureSchedule();
        schedule.setId(id);
        schedule.setAdventure(adventure);
        schedule.setStartDate(start);
        schedule.setEndDate(end);
        schedule.setAvailableSlots(availableSlots);
        schedule.setTotalCapacity(totalCapacity);
        schedule.setAssignedGuideName(guideName);
        schedule.setCheckedInCustomerCount(checkedIn);
        schedule.setStatus(status);
        return schedule;
    }
}