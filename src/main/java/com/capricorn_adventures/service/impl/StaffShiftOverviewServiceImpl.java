package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.StaffShiftOverviewResponseDTO;
import com.capricorn_adventures.entity.StaffDepartment;
import com.capricorn_adventures.entity.StaffShift;
import com.capricorn_adventures.repository.StaffShiftRepository;
import com.capricorn_adventures.service.StaffShiftOverviewService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffShiftOverviewServiceImpl implements StaffShiftOverviewService {

    private static final int REFRESH_INTERVAL_SECONDS = 60;

    private final StaffShiftRepository staffShiftRepository;

    public StaffShiftOverviewServiceImpl(StaffShiftRepository staffShiftRepository) {
        this.staffShiftRepository = staffShiftRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public StaffShiftOverviewResponseDTO getCurrentShiftOverview() {
        LocalDateTime now = LocalDateTime.now();
        List<StaffShift> onShift = staffShiftRepository.findCurrentShifts(now);
        Map<StaffDepartment, List<StaffShift>> byDepartment = onShift.stream()
                .collect(Collectors.groupingBy(StaffShift::getDepartment));

        StaffShiftOverviewResponseDTO response = new StaffShiftOverviewResponseDTO();
        response.setGeneratedAt(now);
        response.setAutoRefreshEnabled(true);
        response.setRefreshIntervalSeconds(REFRESH_INTERVAL_SECONDS);
        response.setDepartments(Arrays.stream(StaffDepartment.values())
                .map(department -> mapDepartment(department, byDepartment.getOrDefault(department, List.of())))
                .toList());

        if (isOwnerOrAdminViewer()) {
            response.setOwnerMetrics(buildOwnerMetrics(now));
        }

        return response;
    }

    private StaffShiftOverviewResponseDTO.DepartmentShiftDTO mapDepartment(StaffDepartment department,
                                                                           List<StaffShift> shifts) {
        StaffShiftOverviewResponseDTO.DepartmentShiftDTO dto = new StaffShiftOverviewResponseDTO.DepartmentShiftDTO();
        dto.setDepartmentCode(department.name());
        dto.setDepartmentName(department.getDisplayName());
        dto.setUnderstaffed(shifts.isEmpty());
        dto.setWarning(shifts.isEmpty() ? "Understaffed" : null);
        dto.setOnShiftStaff(shifts.stream()
                .map(this::mapStaff)
                .toList());
        return dto;
    }

    private StaffShiftOverviewResponseDTO.OnShiftStaffDTO mapStaff(StaffShift shift) {
        StaffShiftOverviewResponseDTO.OnShiftStaffDTO dto = new StaffShiftOverviewResponseDTO.OnShiftStaffDTO();
        dto.setShiftId(shift.getId());
        dto.setStaffId(shift.getStaff().getId());
        dto.setStaffName(resolveName(shift));
        dto.setShiftStartAt(shift.getShiftStartAt());
        dto.setCurrentTaskAssignment(shift.getCurrentTaskAssignment());
        dto.setLastActivityAt(shift.getLastActivityAt());
        return dto;
    }

    private String resolveName(StaffShift shift) {
        String firstName = shift.getStaff().getFirstName() == null ? "" : shift.getStaff().getFirstName().trim();
        String lastName = shift.getStaff().getLastName() == null ? "" : shift.getStaff().getLastName().trim();
        String full = (firstName + " " + lastName).trim();
        return full.isEmpty() ? shift.getStaff().getEmail() : full;
    }

    private StaffShiftOverviewResponseDTO.OwnerShiftMetricsDTO buildOwnerMetrics(LocalDateTime now) {
        LocalDate businessDate = now.toLocalDate();
        LocalDateTime dayStart = businessDate.atStartOfDay();
        LocalDateTime nextDayStart = businessDate.plusDays(1).atStartOfDay();

        List<StaffShift> todayShifts = staffShiftRepository.findShiftsStartingWithinDay(dayStart, nextDayStart);

        double totalHours = todayShifts.stream()
                .mapToDouble(shift -> calculateHours(shift, now))
                .sum();

        BigDecimal estimatedCost = todayShifts.stream()
                .map(shift -> calculateCost(shift, now))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        StaffShiftOverviewResponseDTO.OwnerShiftMetricsDTO metrics = new StaffShiftOverviewResponseDTO.OwnerShiftMetricsDTO();
        metrics.setBusinessDate(businessDate);
        metrics.setTotalLaborHours(roundHours(totalHours));
        metrics.setEstimatedShiftCost(estimatedCost);
        return metrics;
    }

    private double calculateHours(StaffShift shift, LocalDateTime now) {
        LocalDateTime end = shift.getShiftEndAt();
        if (end == null || end.isAfter(now)) {
            end = now;
        }
        if (end.isBefore(shift.getShiftStartAt())) {
            return 0;
        }
        long minutes = Duration.between(shift.getShiftStartAt(), end).toMinutes();
        return Math.max(minutes, 0) / 60.0;
    }

    private BigDecimal calculateCost(StaffShift shift, LocalDateTime now) {
        if (shift.getHourlyRate() == null) {
            return BigDecimal.ZERO;
        }
        return shift.getHourlyRate().multiply(BigDecimal.valueOf(calculateHours(shift, now)));
    }

    private double roundHours(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean isOwnerOrAdminViewer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_OWNER".equals(authority) || "ROLE_ADMIN".equals(authority));
    }
}
