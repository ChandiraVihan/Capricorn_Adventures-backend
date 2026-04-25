package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.ManagerOperationsDashboardResponseDTO;

public interface ManagerOperationsDashboardService {
    ManagerOperationsDashboardResponseDTO getDashboard();

    ManagerOperationsDashboardResponseDTO.TourSlotDTO assignGuide(Long scheduleId, String guideName);
}