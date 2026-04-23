package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;
import com.capricorn_adventures.dto.StaffShiftOverviewResponseDTO;
import com.capricorn_adventures.service.ManagerOperationsDashboardService;
import com.capricorn_adventures.service.StaffShiftOverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/operations")
@CrossOrigin(origins = "*")
public class ManagerOperationsDashboardController {

    private final ManagerOperationsDashboardService dashboardService;
    private final StaffShiftOverviewService staffShiftOverviewService;

    public ManagerOperationsDashboardController(ManagerOperationsDashboardService dashboardService,
                                                StaffShiftOverviewService staffShiftOverviewService) {
        this.dashboardService = dashboardService;
        this.staffShiftOverviewService = staffShiftOverviewService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ManagerOperationsDashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/shift-overview")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<StaffShiftOverviewResponseDTO> getShiftOverview() {
        return ResponseEntity.ok(staffShiftOverviewService.getCurrentShiftOverview());
    }
}
