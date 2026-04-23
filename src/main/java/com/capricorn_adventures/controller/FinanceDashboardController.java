package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.ProfitLossDashboardResponseDTO;
import com.capricorn_adventures.service.ProfitLossDashboardService;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "*")
public class FinanceDashboardController {

    private final ProfitLossDashboardService profitLossDashboardService;

    public FinanceDashboardController(ProfitLossDashboardService profitLossDashboardService) {
        this.profitLossDashboardService = profitLossDashboardService;
    }

    @GetMapping("/pnl")
    public ResponseEntity<ProfitLossDashboardResponseDTO> getProfitAndLoss(
            @RequestParam(value = "month", required = false) String month) {
        return ResponseEntity.ok(profitLossDashboardService.getDashboard(parseMonth(month)));
    }

    @GetMapping("/pnl/export")
    public ResponseEntity<byte[]> exportProfitAndLoss(
            @RequestParam(value = "month", required = false) String month) {
        YearMonth resolvedMonth = parseMonth(month);
        byte[] file = profitLossDashboardService.exportDashboard(resolvedMonth);
        String suffix = resolvedMonth == null ? "mtd" : resolvedMonth.toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=profit-loss-" + suffix + ".xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return null;
        }

        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("month must be in YYYY-MM format");
        }
    }
}
