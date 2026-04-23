package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.capricorn_adventures.dto.ProfitLossDashboardResponseDTO;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.BookingRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfitLossDashboardServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AdventureCheckoutBookingRepository adventureCheckoutBookingRepository;

    private ProfitLossDashboardServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProfitLossDashboardServiceImpl(bookingRepository, adventureCheckoutBookingRepository);
        ReflectionTestUtils.setField(service, "hotelCostRate", BigDecimal.valueOf(0.45));
        ReflectionTestUtils.setField(service, "adventureCostRate", BigDecimal.valueOf(0.50));
        ReflectionTestUtils.setField(service, "thirdPartyCommissionRate", BigDecimal.valueOf(0.12));
        ReflectionTestUtils.setField(service, "taxRate", BigDecimal.valueOf(0.18));
        ReflectionTestUtils.setField(service, "monthlyRevenueBudget", BigDecimal.valueOf(1000));
        ReflectionTestUtils.setField(service, "monthlyCostBudget", BigDecimal.valueOf(500));
        ReflectionTestUtils.setField(service, "monthlyGrossMarginBudget", BigDecimal.valueOf(500));
        ReflectionTestUtils.setField(service, "monthlyNetProfitBudget", BigDecimal.valueOf(400));
    }

    @Test
    void getDashboard_calculatesSummaryBreakdownAndTax() {
        Booking hotelOne = hotelBooking(BigDecimal.valueOf(1000), BigDecimal.valueOf(100));
        Booking hotelTwo = hotelBooking(BigDecimal.valueOf(500), BigDecimal.ZERO);
        AdventureCheckoutBooking adventureOne = adventureBooking(BigDecimal.valueOf(2000), BigDecimal.valueOf(200));

        when(bookingRepository.findByCheckInDateBetweenAndStatusIn(any(LocalDate.class), any(LocalDate.class), anyList()))
                .thenReturn(List.of(hotelOne, hotelTwo));
        when(adventureCheckoutBookingRepository.findByCreatedAtBetweenAndStatusIn(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of(adventureOne));

        ProfitLossDashboardResponseDTO response = service.getDashboard(YearMonth.of(2026, 4));

        assertEquals(new BigDecimal("3200.00"), response.getRevenue().getActual());
        assertEquals(new BigDecimal("1746.00"), response.getCostOfSales().getActual());
        assertEquals(new BigDecimal("1454.00"), response.getGrossMargin().getActual());
        assertEquals(new BigDecimal("1454.00"), response.getNetProfitPreTax().getActual());
        assertEquals(new BigDecimal("1192.28"), response.getNetProfitPostTax().getActual());

        assertEquals(new BigDecimal("1400.00"), response.getProductBreakdown().getHotelRevenue());
        assertEquals(new BigDecimal("1800.00"), response.getProductBreakdown().getAdventureRevenue());
        assertEquals(new BigDecimal("216.00"), response.getProductBreakdown().getThirdPartyCommission());

        assertEquals(new BigDecimal("18.00"), response.getTaxSummary().getTaxRatePercent());
        assertEquals(new BigDecimal("261.72"), response.getTaxSummary().getTaxAmount());
        assertTrue(response.getLineItems().size() >= 10);
    }

    @Test
    void exportDashboard_returnsExcelBytes() {
        when(bookingRepository.findByCheckInDateBetweenAndStatusIn(any(LocalDate.class), any(LocalDate.class), anyList()))
                .thenReturn(List.of());
        when(adventureCheckoutBookingRepository.findByCreatedAtBetweenAndStatusIn(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(List.of());

        byte[] output = service.exportDashboard(YearMonth.of(2026, 4));
        assertTrue(output.length > 0);
    }

    private Booking hotelBooking(BigDecimal totalPrice, BigDecimal refunded) {
        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(totalPrice);
        booking.setRefundedAmount(refunded);
        return booking;
    }

    private AdventureCheckoutBooking adventureBooking(BigDecimal totalPrice, BigDecimal refunded) {
        AdventureCheckoutBooking booking = new AdventureCheckoutBooking();
        booking.setStatus(AdventureCheckoutStatus.CONFIRMED);
        booking.setTotalPrice(totalPrice);
        booking.setRefundedAmount(refunded);
        return booking;
    }
}
