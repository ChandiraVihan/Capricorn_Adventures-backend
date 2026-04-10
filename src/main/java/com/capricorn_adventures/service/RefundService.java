package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.*;
import com.capricorn_adventures.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class RefundService {

    private final BookingRepository bookingRepository;
    private final AdventureCheckoutBookingRepository adventureBookingRepository;
    private final RefundTransactionRepository refundTransactionRepository;
    private final PayHereGatewayService payHereGatewayService;
    private final CancellationPolicyService cancellationPolicyService;
    private final NotificationService notificationService;

    public RefundService(BookingRepository bookingRepository,
                         AdventureCheckoutBookingRepository adventureBookingRepository,
                         RefundTransactionRepository refundTransactionRepository,
                         PayHereGatewayService payHereGatewayService,
                         CancellationPolicyService cancellationPolicyService,
                         NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.adventureBookingRepository = adventureBookingRepository;
        this.refundTransactionRepository = refundTransactionRepository;
        this.payHereGatewayService = payHereGatewayService;
        this.cancellationPolicyService = cancellationPolicyService;
        this.notificationService = notificationService;
    }

    @Transactional
    public void processRoomRefund(Long bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REFUNDED) {
            throw new RuntimeException("Booking already processed for cancellation or refund");
        }

        BigDecimal refundAmount = cancellationPolicyService.calculateRefundAmount(booking);
        
        RefundTransaction transaction = new RefundTransaction();
        transaction.setRoomBooking(booking);
        transaction.setAmount(refundAmount);
        transaction.setType(refundAmount.compareTo(booking.getTotalPrice()) == 0 ? 
            RefundTransaction.RefundType.FULL : RefundTransaction.RefundType.PARTIAL);
        transaction.setStatus(RefundTransaction.RefundStatus.PENDING);
        refundTransactionRepository.save(transaction);

        // Try PayHere gateway - but don't block the refund if gateway is unavailable
        try {
            Map<String, Object> gatewayResponse = payHereGatewayService.processRefund(
                booking.getPaymentReference(), refundAmount, reason);

            if (gatewayResponse != null && "1".equals(String.valueOf(gatewayResponse.get("status")))) {
                transaction.setStatus(RefundTransaction.RefundStatus.SUCCESS);
            } else {
                // Gateway rejected or returned unexpected response - keep as PENDING for manual review
                transaction.setFailureReason(gatewayResponse != null ? 
                    String.valueOf(gatewayResponse.get("msg")) : "Gateway unavailable");
            }
        } catch (Exception e) {
            // Gateway unreachable - keep transaction as PENDING for manual processing
            transaction.setFailureReason("Gateway error: " + e.getMessage());
        }
        refundTransactionRepository.save(transaction);

        // Always update booking status regardless of gateway result
        booking.setStatus(refundAmount.compareTo(booking.getTotalPrice()) == 0 ? 
            BookingStatus.REFUNDED : BookingStatus.PARTIALLY_REFUNDED);
        booking.setRefundedAmount(refundAmount);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        bookingRepository.save(booking);

        try {
            notificationService.sendRefundConfirmation(booking);
        } catch (Exception e) {
            // Don't fail the refund if email fails
        }
    }

    @Transactional
    public void processAdventureRefund(Long bookingId, String reason) {
        AdventureCheckoutBooking booking = adventureBookingRepository.findById(bookingId)
            .orElseThrow(() -> new RuntimeException("Adventure booking not found: " + bookingId));

        if (booking.getStatus() == AdventureCheckoutStatus.CANCELLED || booking.getStatus() == AdventureCheckoutStatus.REFUNDED) {
            throw new RuntimeException("Adventure booking already processed");
        }

        BigDecimal refundAmount = cancellationPolicyService.calculateRefundAmount(booking);
        
        RefundTransaction transaction = new RefundTransaction();
        transaction.setAdventureBooking(booking);
        transaction.setAmount(refundAmount);
        transaction.setType(refundAmount.compareTo(booking.getTotalPrice()) == 0 ? 
            RefundTransaction.RefundType.FULL : RefundTransaction.RefundType.PARTIAL);
        transaction.setStatus(RefundTransaction.RefundStatus.PENDING);
        refundTransactionRepository.save(transaction);

        try {
            Map<String, Object> gatewayResponse = payHereGatewayService.processRefund(
                booking.getPaymentReference(), refundAmount, reason);

            if (gatewayResponse != null && "1".equals(String.valueOf(gatewayResponse.get("status")))) {
                transaction.setStatus(RefundTransaction.RefundStatus.SUCCESS);
            } else {
                transaction.setFailureReason(gatewayResponse != null ? 
                    String.valueOf(gatewayResponse.get("msg")) : "Gateway unavailable");
            }
        } catch (Exception e) {
            transaction.setFailureReason("Gateway error: " + e.getMessage());
        }
        refundTransactionRepository.save(transaction);

        booking.setStatus(refundAmount.compareTo(booking.getTotalPrice()) == 0 ? 
            AdventureCheckoutStatus.REFUNDED : AdventureCheckoutStatus.PARTIALLY_REFUNDED);
        booking.setRefundedAmount(refundAmount);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        adventureBookingRepository.save(booking);

        try {
            notificationService.sendRefundConfirmation(booking);
        } catch (Exception e) {
            // Don't fail the refund if email fails
        }
    }
}
