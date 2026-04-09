package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InvoiceResponseDTO {

    private Long id;
    private String invoiceNumber;
    private String bookingReferenceId;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private LocalDateTime issuedAt;

    public static InvoiceResponseDTO from(com.capricorn_adventures.entity.Invoice inv) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.id                 = inv.getId();
        dto.invoiceNumber      = inv.getInvoiceNumber();
        dto.bookingReferenceId = inv.getBooking().getReferenceId();
        dto.subtotal           = inv.getSubtotal();
        dto.taxAmount          = inv.getTaxAmount();
        dto.totalAmount        = inv.getTotalAmount();
        dto.status             = inv.getStatus();
        dto.issuedAt           = inv.getIssuedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getBookingReferenceId() { return bookingReferenceId; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public InvoiceStatus getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
}
