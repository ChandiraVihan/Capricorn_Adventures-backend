package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.AssignGuideRequestDTO;
import com.capricorn_adventures.dto.CreateStaffShiftRequestDTO;
import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;
import com.capricorn_adventures.dto.StaffShiftResponseDTO;
import com.capricorn_adventures.dto.StaffShiftOverviewResponseDTO;
import com.capricorn_adventures.service.ManagerOperationsDashboardService;
import com.capricorn_adventures.service.StaffShiftOverviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<StaffShiftOverviewResponseDTO> getShiftOverview() {
        return ResponseEntity.ok(staffShiftOverviewService.getCurrentShiftOverview());
    }

    @PatchMapping("/tours/{scheduleId}/assign-guide")
    public ResponseEntity<ManagerOperationsDashboardResponseDTO.TourSlotDTO> assignGuide(
            @PathVariable Long scheduleId,
            @Valid @RequestBody AssignGuideRequestDTO request) {
        return ResponseEntity.ok(dashboardService.assignGuide(scheduleId, request.getGuideName()));
    }

    @PostMapping("/shifts")
    public ResponseEntity<StaffShiftResponseDTO> createShift(@Valid @RequestBody CreateStaffShiftRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(staffShiftOverviewService.createShift(request));
    }
}
