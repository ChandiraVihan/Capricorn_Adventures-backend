package com.capricorn_adventures.controller;


import com.capricorn_adventures.entity.Invoice;
import com.capricorn_adventures.entity.Payment;
import com.capricorn_adventures.service.PaymentInvoiceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
public class PaymentInvoiceController {

    private final PaymentInvoiceService service;

    public PaymentInvoiceController(PaymentInvoiceService service) {
        this.service = service;
    }

    // AC3 — Finance export by date range
    @GetMapping("/payments")
    public ResponseEntity<List<Payment>> getPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(service.getPaymentsByDateRange(from, to));
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getInvoices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(service.getInvoicesByDateRange(from, to));
    }

    // AC2 — Manual refund update
    @PutMapping("/payments/{paymentId}/refund")
    public ResponseEntity<Payment> markRefund(@PathVariable Long paymentId) {
        return ResponseEntity.ok(service.recordRefund(paymentId));
    }

    // Get payment & invoice by booking reference
    @GetMapping("/bookings/{referenceId}/payment")
    public ResponseEntity<Payment> getPaymentByBooking(@PathVariable String referenceId) {
        return ResponseEntity.ok(service.getPaymentByBookingReference(referenceId));
    }

    @GetMapping("/bookings/{referenceId}/invoice")
    public ResponseEntity<Invoice> getInvoiceByBooking(@PathVariable String referenceId) {
        return ResponseEntity.ok(service.getInvoiceByBookingReference(referenceId));
    }
}
