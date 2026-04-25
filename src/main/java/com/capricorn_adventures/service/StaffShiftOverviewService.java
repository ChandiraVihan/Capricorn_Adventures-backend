package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.CreateStaffShiftRequestDTO;
import com.capricorn_adventures.dto.StaffShiftResponseDTO;
import com.capricorn_adventures.dto.StaffShiftOverviewResponseDTO;

public interface StaffShiftOverviewService {
    StaffShiftOverviewResponseDTO getCurrentShiftOverview();

    StaffShiftResponseDTO createShift(CreateStaffShiftRequestDTO request);
}
