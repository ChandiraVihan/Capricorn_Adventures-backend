package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;
import com.capricorn_adventures.service.ManagerOperationsDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/manager/operations")
@CrossOrigin(origins = "*")
public class ManagerOperationsDashboardController {

    private final ManagerOperationsDashboardService dashboardService;

    public ManagerOperationsDashboardController(ManagerOperationsDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ManagerOperationsDashboardResponseDTO> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}