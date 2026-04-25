package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.StaffShiftOverviewResponseDTO;
import com.capricorn_adventures.entity.StaffDepartment;
import com.capricorn_adventures.entity.StaffShift;
import com.capricorn_adventures.entity.User;
import com.capricorn_adventures.repository.StaffShiftRepository;
import com.capricorn_adventures.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class StaffShiftOverviewServiceImplTest {

    @Mock
    private StaffShiftRepository staffShiftRepository;

        @Mock
        private UserRepository userRepository;

    private StaffShiftOverviewServiceImpl service;

    @BeforeEach
    void setUp() {
                service = new StaffShiftOverviewServiceImpl(staffShiftRepository, userRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentShiftOverview_groupsByDepartmentAndMarksUnderstaffed() {
        StaffShift roomServiceShift = shift(
                1L,
                StaffDepartment.ROOM_SERVICE,
                "Nimal",
                "Silva",
                "staff1@capricorn.com",
                LocalDateTime.now().minusHours(1),
                null,
                "Deliver order #102",
                LocalDateTime.now().minusMinutes(4),
                new BigDecimal("12.50")
        );

        StaffShift laundryShift = shift(
                2L,
                StaffDepartment.LAUNDRY,
                "Saman",
                "Perera",
                "staff2@capricorn.com",
                LocalDateTime.now().minusHours(2),
                null,
                "Collect bedding from floor 2",
                LocalDateTime.now().minusMinutes(7),
                new BigDecimal("11.00")
        );

        when(staffShiftRepository.findCurrentShifts(any())).thenReturn(List.of(roomServiceShift, laundryShift));

        StaffShiftOverviewResponseDTO response = service.getCurrentShiftOverview();

        assertTrue(response.isAutoRefreshEnabled());
        assertEquals(60, response.getRefreshIntervalSeconds());
        assertEquals(3, response.getDepartments().size());

        StaffShiftOverviewResponseDTO.DepartmentShiftDTO housekeeping = response.getDepartments().stream()
                .filter(dept -> "HOUSEKEEPING".equals(dept.getDepartmentCode()))
                .findFirst()
                .orElseThrow();

        assertTrue(housekeeping.isUnderstaffed());
        assertEquals("Understaffed", housekeeping.getWarning());

        StaffShiftOverviewResponseDTO.DepartmentShiftDTO roomService = response.getDepartments().stream()
                .filter(dept -> "ROOM_SERVICE".equals(dept.getDepartmentCode()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, roomService.getOnShiftStaff().size());
        assertEquals("Nimal Silva", roomService.getOnShiftStaff().get(0).getStaffName());
        assertNotNull(roomService.getOnShiftStaff().get(0).getCurrentTaskAssignment());
        assertNotNull(roomService.getOnShiftStaff().get(0).getLastActivityAt());
    }

    @Test
    void getCurrentShiftOverview_ownerSeesLaborHoursAndShiftCost() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("owner", "n/a", List.of(() -> "ROLE_OWNER"))
        );

        when(staffShiftRepository.findCurrentShifts(any())).thenReturn(List.of());

        StaffShift finishedShift = shift(
                3L,
                StaffDepartment.HOUSEKEEPING,
                "Kamal",
                "Fernando",
                "staff3@capricorn.com",
                LocalDateTime.now().minusHours(4),
                LocalDateTime.now().minusHours(1),
                "Room turnover",
                LocalDateTime.now().minusHours(1),
                new BigDecimal("10.00")
        );

        when(staffShiftRepository.findShiftsStartingWithinDay(any(), any())).thenReturn(List.of(finishedShift));

        StaffShiftOverviewResponseDTO response = service.getCurrentShiftOverview();

        assertNotNull(response.getOwnerMetrics());
        assertTrue(response.getOwnerMetrics().getTotalLaborHours() > 0);
        assertTrue(response.getOwnerMetrics().getEstimatedShiftCost().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getCurrentShiftOverview_managerDoesNotSeeOwnerMetrics() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("manager", "n/a", List.of(() -> "ROLE_MANAGER"))
        );

        when(staffShiftRepository.findCurrentShifts(any())).thenReturn(List.of());

        StaffShiftOverviewResponseDTO response = service.getCurrentShiftOverview();

        assertNull(response.getOwnerMetrics());
    }

    private StaffShift shift(Long shiftId,
                             StaffDepartment department,
                             String firstName,
                             String lastName,
                             String email,
                             LocalDateTime start,
                             LocalDateTime end,
                             String task,
                             LocalDateTime lastActivity,
                             BigDecimal hourlyRate) {
        User staff = User.builder()
                .id(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .role(User.UserRole.STAFF)
                .status(User.UserStatus.ACTIVE)
                .build();

        StaffShift shift = new StaffShift();
        shift.setId(shiftId);
        shift.setStaff(staff);
        shift.setDepartment(department);
        shift.setShiftStartAt(start);
        shift.setShiftEndAt(end);
        shift.setCurrentTaskAssignment(task);
        shift.setLastActivityAt(lastActivity);
        shift.setHourlyRate(hourlyRate);
        return shift;
    }
}
