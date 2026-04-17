package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.ProfitLossDashboardResponseDTO;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.service.ProfitLossDashboardService;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProfitLossDashboardServiceImpl implements ProfitLossDashboardService {

    private static final String GREEN = "green";
    private static final String RED = "red";

    private final BookingRepository bookingRepository;
    private final AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;

    @Value("${app.finance.hotel-cost-rate:0.45}")
    private BigDecimal hotelCostRate;

    @Value("${app.finance.adventure-cost-rate:0.50}")
    private BigDecimal adventureCostRate;

    @Value("${app.finance.third-party-commission-rate:0.12}")
    private BigDecimal thirdPartyCommissionRate;

    @Value("${app.finance.tax-rate:0.18}")
    private BigDecimal taxRate;

    @Value("${app.finance.monthly-revenue-budget:1000000}")
    private BigDecimal monthlyRevenueBudget;

    @Value("${app.finance.monthly-cost-budget:550000}")
    private BigDecimal monthlyCostBudget;

    @Value("${app.finance.monthly-gross-margin-budget:450000}")
    private BigDecimal monthlyGrossMarginBudget;

    @Value("${app.finance.monthly-net-profit-budget:300000}")
    private BigDecimal monthlyNetProfitBudget;

    public ProfitLossDashboardServiceImpl(BookingRepository bookingRepository,
                                          AdventureCheckoutBookingRepository adventureCheckoutBookingRepository) {
        this.bookingRepository = bookingRepository;
        this.adventureCheckoutBookingRepository = adventureCheckoutBookingRepository;
    }

    @Override
    public ProfitLossDashboardResponseDTO getDashboard(YearMonth monthFilter) {
        PeriodRange currentRange = resolveCurrentRange(monthFilter);
        PeriodRange previousRange = resolvePreviousRange(currentRange, monthFilter == null);

        Computation current = computeForRange(currentRange.startDate(), currentRange.endDate(),
                currentRange.startDateTime(), currentRange.endDateTime());
        Computation previous = computeForRange(previousRange.startDate(), previousRange.endDate(),
                previousRange.startDateTime(), previousRange.endDateTime());

        ProfitLossDashboardResponseDTO dto = new ProfitLossDashboardResponseDTO();
        dto.setMonth(currentRange.month().toString());
        dto.setMonthToDate(monthFilter == null);
        dto.setGeneratedAt(LocalDateTime.now());

        dto.setRevenue(buildCard("Revenue", current.totalRevenue, monthlyRevenueBudget, previous.totalRevenue));
        dto.setCostOfSales(buildCard("Cost of Sales", current.totalCostOfSales, monthlyCostBudget, previous.totalCostOfSales));
        dto.setGrossMargin(buildCard("Gross Margin", current.grossMargin, monthlyGrossMarginBudget, previous.grossMargin));
        dto.setNetProfitPreTax(buildCard("Net Profit (Pre-tax)", current.netProfitPreTax, monthlyNetProfitBudget, previous.netProfitPreTax));
        dto.setNetProfitPostTax(buildCard("Net Profit (Post-tax)", current.netProfitPostTax, monthlyNetProfitBudget, previous.netProfitPostTax));

        ProfitLossDashboardResponseDTO.ProductBreakdownDTO breakdown = new ProfitLossDashboardResponseDTO.ProductBreakdownDTO();
        breakdown.setHotelRevenue(scale(current.hotelRevenue));
        breakdown.setAdventureRevenue(scale(current.adventureRevenue));
        breakdown.setThirdPartyCommission(scale(current.thirdPartyCommission));
        dto.setProductBreakdown(breakdown);

        ProfitLossDashboardResponseDTO.TaxSummaryDTO taxSummary = new ProfitLossDashboardResponseDTO.TaxSummaryDTO();
        taxSummary.setTaxRatePercent(scale(taxRate.multiply(BigDecimal.valueOf(100))));
        taxSummary.setTaxAmount(scale(current.taxAmount));
        taxSummary.setNetProfitPreTax(scale(current.netProfitPreTax));
        taxSummary.setNetProfitPostTax(scale(current.netProfitPostTax));
        dto.setTaxSummary(taxSummary);

        dto.setLineItems(buildLineItems(current));
        return dto;
    }

    @Override
    public byte[] exportDashboard(YearMonth monthFilter) {
        ProfitLossDashboardResponseDTO dashboard = getDashboard(monthFilter);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = (XSSFSheet) workbook.createSheet("P&L Dashboard");
            DataFormat dataFormat = workbook.createDataFormat();

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle amountStyle = workbook.createCellStyle();
            amountStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
            amountStyle.setBorderBottom(BorderStyle.THIN);
            amountStyle.setBorderTop(BorderStyle.THIN);
            amountStyle.setBorderLeft(BorderStyle.THIN);
            amountStyle.setBorderRight(BorderStyle.THIN);

            CellStyle textStyle = workbook.createCellStyle();
            textStyle.setBorderBottom(BorderStyle.THIN);
            textStyle.setBorderTop(BorderStyle.THIN);
            textStyle.setBorderLeft(BorderStyle.THIN);
            textStyle.setBorderRight(BorderStyle.THIN);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.LEFT);

            int rowNum = 0;
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Profit & Loss Dashboard - " + dashboard.getMonth());
            titleCell.setCellStyle(titleStyle);

            Row metaRow = sheet.createRow(rowNum++);
            metaRow.createCell(0).setCellValue("Generated At");
            metaRow.createCell(1).setCellValue(String.valueOf(dashboard.getGeneratedAt()));

            rowNum++;

            Row headerRow = sheet.createRow(rowNum++);
            createCell(headerRow, 0, "Category", headerStyle);
            createCell(headerRow, 1, "Line Item", headerStyle);
            createCell(headerRow, 2, "Amount", headerStyle);

            for (ProfitLossDashboardResponseDTO.LineItemDTO item : dashboard.getLineItems()) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, item.getCategory(), textStyle);
                createCell(row, 1, item.getLabel(), textStyle);
                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(item.getAmount().doubleValue());
                amountCell.setCellStyle(amountStyle);
            }

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);
            sheet.autoSizeColumn(2);

            workbook.write(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate P&L Excel export", ex);
        }
    }

    private void createCell(Row row, int index, String value, CellStyle style) {
        Cell cell = row.createCell(index);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private ProfitLossDashboardResponseDTO.SummaryCardDTO buildCard(String title,
                                                                     BigDecimal actual,
                                                                     BigDecimal budget,
                                                                     BigDecimal previousMonthValue) {
        BigDecimal safeBudget = budget == null ? BigDecimal.ZERO : budget;
        BigDecimal variance = actual.subtract(safeBudget);

        ProfitLossDashboardResponseDTO.SummaryCardDTO card = new ProfitLossDashboardResponseDTO.SummaryCardDTO();
        card.setTitle(title);
        card.setActual(scale(actual));
        card.setBudget(scale(safeBudget));
        card.setVariance(scale(variance));
        card.setVarianceColor(variance.compareTo(BigDecimal.ZERO) >= 0 ? GREEN : RED);
        card.setMonthOverMonthChangePercent(scale(percentChange(actual, previousMonthValue)));
        return card;
    }

    private List<ProfitLossDashboardResponseDTO.LineItemDTO> buildLineItems(Computation current) {
        List<ProfitLossDashboardResponseDTO.LineItemDTO> lineItems = new ArrayList<>();
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Revenue", "Hotel Revenue", scale(current.hotelRevenue)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Revenue", "Adventure Revenue", scale(current.adventureRevenue)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Revenue", "Total Revenue", scale(current.totalRevenue)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Cost of Sales", "Hotel Cost of Sales", scale(current.hotelCost)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Cost of Sales", "Adventure Cost of Sales", scale(current.adventureCost)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Cost of Sales", "Third-Party Commission", scale(current.thirdPartyCommission)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Cost of Sales", "Total Cost of Sales", scale(current.totalCostOfSales)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Profit", "Gross Margin", scale(current.grossMargin)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Profit", "Net Profit (Pre-tax)", scale(current.netProfitPreTax)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Tax", "Tax", scale(current.taxAmount)));
        lineItems.add(new ProfitLossDashboardResponseDTO.LineItemDTO("Profit", "Net Profit (Post-tax)", scale(current.netProfitPostTax)));
        return lineItems;
    }

    private Computation computeForRange(LocalDate startDate,
                                        LocalDate endDate,
                                        LocalDateTime startDateTime,
                                        LocalDateTime endDateTime) {
        List<BookingStatus> hotelRevenueStatuses = List.of(
                BookingStatus.CONFIRMED,
                BookingStatus.REFUNDED,
                BookingStatus.PARTIALLY_REFUNDED
        );
        List<AdventureCheckoutStatus> adventureRevenueStatuses = List.of(
                AdventureCheckoutStatus.CONFIRMED,
                AdventureCheckoutStatus.REFUNDED,
                AdventureCheckoutStatus.PARTIALLY_REFUNDED
        );

        List<Booking> hotelBookings = bookingRepository.findByCheckInDateBetweenAndStatusIn(
                startDate,
                endDate,
                hotelRevenueStatuses
        );

        List<AdventureCheckoutBooking> adventureBookings = adventureCheckoutBookingRepository
                .findByCreatedAtBetweenAndStatusIn(startDateTime, endDateTime, adventureRevenueStatuses);

        BigDecimal hotelRevenue = hotelBookings.stream()
                .map(this::netBookingRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal adventureRevenue = adventureBookings.stream()
                .map(this::netAdventureRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal thirdPartyCommission = adventureRevenue.multiply(thirdPartyCommissionRate);
        BigDecimal hotelCost = hotelRevenue.multiply(hotelCostRate);
        BigDecimal adventureCost = adventureRevenue.multiply(adventureCostRate);
        BigDecimal totalRevenue = hotelRevenue.add(adventureRevenue);
        BigDecimal totalCostOfSales = hotelCost.add(adventureCost).add(thirdPartyCommission);
        BigDecimal grossMargin = totalRevenue.subtract(totalCostOfSales);
        BigDecimal netProfitPreTax = grossMargin;
        BigDecimal taxAmount = netProfitPreTax.compareTo(BigDecimal.ZERO) > 0
                ? netProfitPreTax.multiply(taxRate)
                : BigDecimal.ZERO;
        BigDecimal netProfitPostTax = netProfitPreTax.subtract(taxAmount);

        return new Computation(
                hotelRevenue,
                adventureRevenue,
                totalRevenue,
                hotelCost,
                adventureCost,
                thirdPartyCommission,
                totalCostOfSales,
                grossMargin,
                netProfitPreTax,
                taxAmount,
                netProfitPostTax
        );
    }

    private BigDecimal netBookingRevenue(Booking booking) {
        BigDecimal totalPrice = booking.getTotalPrice() == null ? BigDecimal.ZERO : booking.getTotalPrice();
        BigDecimal refunded = booking.getRefundedAmount() == null ? BigDecimal.ZERO : booking.getRefundedAmount();
        BigDecimal net = totalPrice.subtract(refunded);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net;
    }

    private BigDecimal netAdventureRevenue(AdventureCheckoutBooking booking) {
        BigDecimal totalPrice = booking.getTotalPrice() == null ? BigDecimal.ZERO : booking.getTotalPrice();
        BigDecimal refunded = booking.getRefundedAmount() == null ? BigDecimal.ZERO : booking.getRefundedAmount();
        BigDecimal net = totalPrice.subtract(refunded);
        return net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net;
    }

    private PeriodRange resolveCurrentRange(YearMonth monthFilter) {
        if (monthFilter == null) {
            LocalDate today = LocalDate.now();
            YearMonth currentMonth = YearMonth.from(today);
            return new PeriodRange(
                    currentMonth,
                    currentMonth.atDay(1),
                    today,
                    currentMonth.atDay(1).atStartOfDay(),
                    today.plusDays(1).atStartOfDay()
            );
        }

        return new PeriodRange(
                monthFilter,
                monthFilter.atDay(1),
                monthFilter.atEndOfMonth(),
                monthFilter.atDay(1).atStartOfDay(),
                monthFilter.plusMonths(1).atDay(1).atStartOfDay()
        );
    }

    private PeriodRange resolvePreviousRange(PeriodRange currentRange, boolean isMonthToDate) {
        YearMonth previousMonth = currentRange.month().minusMonths(1);
        if (isMonthToDate) {
            int day = Math.min(currentRange.endDate().getDayOfMonth(), previousMonth.lengthOfMonth());
            LocalDate previousEnd = previousMonth.atDay(day);
            return new PeriodRange(
                    previousMonth,
                    previousMonth.atDay(1),
                    previousEnd,
                    previousMonth.atDay(1).atStartOfDay(),
                    previousEnd.plusDays(1).atStartOfDay()
            );
        }

        return new PeriodRange(
                previousMonth,
                previousMonth.atDay(1),
                previousMonth.atEndOfMonth(),
                previousMonth.atDay(1).atStartOfDay(),
                currentRange.month().atDay(1).atStartOfDay()
        );
    }

    private BigDecimal percentChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        }

        return current.subtract(previous)
                .divide(previous.abs(), 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private record PeriodRange(YearMonth month,
                               LocalDate startDate,
                               LocalDate endDate,
                               LocalDateTime startDateTime,
                               LocalDateTime endDateTime) {
    }

    private record Computation(BigDecimal hotelRevenue,
                               BigDecimal adventureRevenue,
                               BigDecimal totalRevenue,
                               BigDecimal hotelCost,
                               BigDecimal adventureCost,
                               BigDecimal thirdPartyCommission,
                               BigDecimal totalCostOfSales,
                               BigDecimal grossMargin,
                               BigDecimal netProfitPreTax,
                               BigDecimal taxAmount,
                               BigDecimal netProfitPostTax) {
    }
}
