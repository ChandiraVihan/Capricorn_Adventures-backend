package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.ProfitLossDashboardResponseDTO;
import java.time.YearMonth;

public interface ProfitLossDashboardService {
    ProfitLossDashboardResponseDTO getDashboard(YearMonth monthFilter);
    byte[] exportDashboard(YearMonth monthFilter);
}
