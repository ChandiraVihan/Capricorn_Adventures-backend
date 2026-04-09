package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponseDTO {

    private Long id;
    private String transactionId;
    private String bookingReferenceId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String failureReason;
    private String gatewayMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory from entity
    public static PaymentResponseDTO from(com.capricorn_adventures.entity.Payment p) {
        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.id               = p.getId();
        dto.transactionId    = p.getTransactionId();
        dto.bookingReferenceId = p.getBooking().getReferenceId();
        dto.amount           = p.getAmount();
        dto.currency         = p.getCurrency();
        dto.status           = p.getStatus();
        dto.failureReason    = p.getFailureReason();
        dto.gatewayMethod    = p.getGatewayMethod();
        dto.createdAt        = p.getCreatedAt();
        dto.updatedAt        = p.getUpdatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public String getBookingReferenceId() { return bookingReferenceId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public String getGatewayMethod() { return gatewayMethod; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
