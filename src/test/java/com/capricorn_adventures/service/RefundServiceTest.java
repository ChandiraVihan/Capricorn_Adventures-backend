package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.*;
import com.capricorn_adventures.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RefundServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private AdventureCheckoutBookingRepository adventureBookingRepository;
    @Mock private RefundTransactionRepository refundTransactionRepository;
    @Mock private PayHereGatewayService payHereGatewayService;
    @Mock private CancellationPolicyService cancellationPolicyService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private RefundService refundService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessRoomRefund_Success() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("1000.00"));
        booking.setPaymentReference("PAY123");
        
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(cancellationPolicyService.calculateRefundAmount(booking)).thenReturn(new BigDecimal("1000.00"));
        when(payHereGatewayService.processRefund(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(Map.of("status", 1, "msg", "Success"));

        // Act
        refundService.processRoomRefund(bookingId, "Customer request");

        // Assert
        assertEquals(BookingStatus.REFUNDED, booking.getStatus());
        assertEquals(new BigDecimal("1000.00"), booking.getRefundedAmount());
        verify(notificationService, times(1)).sendRefundConfirmation(booking);
        verify(refundTransactionRepository, times(1)).save(any(RefundTransaction.class));
    }

    @Test
    void testProcessAdventureRefund_Partial_Success() {
        // Arrange
        Long bookingId = 2L;
        AdventureCheckoutBooking booking = new AdventureCheckoutBooking();
        booking.setId(bookingId);
        booking.setStatus(AdventureCheckoutStatus.CONFIRMED);
        booking.setTotalPrice(new BigDecimal("500.00"));
        booking.setPaymentReference("PAY456");

        when(adventureBookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(cancellationPolicyService.calculateRefundAmount(booking)).thenReturn(new BigDecimal("250.00"));
        when(payHereGatewayService.processRefund(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(Map.of("status", 1, "msg", "Success"));

        // Act
        refundService.processAdventureRefund(bookingId, "Policy partial refund");

        // Assert
        assertEquals(AdventureCheckoutStatus.PARTIALLY_REFUNDED, booking.getStatus());
        assertEquals(new BigDecimal("250.00"), booking.getRefundedAmount());
        verify(notificationService, times(1)).sendRefundConfirmation(booking);
    }

    @Test
    void testProcessRoomRefund_GatewayFailure() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentReference("PAY123");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(cancellationPolicyService.calculateRefundAmount(booking)).thenReturn(new BigDecimal("1000.00"));
        when(payHereGatewayService.processRefund(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(Map.of("status", -1, "msg", "Gateway error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            refundService.processRoomRefund(bookingId, "Customer request");
        });

        assertTrue(exception.getMessage().contains("Gateway error"));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }
}
