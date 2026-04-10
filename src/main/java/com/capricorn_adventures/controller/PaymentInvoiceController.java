package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.InvoiceResponseDTO;
import com.capricorn_adventures.dto.PaymentResponseDTO;
import com.capricorn_adventures.service.PaymentInvoiceService;
import com.capricorn_adventures.service.WebhookService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
public class PaymentInvoiceController {

    private final PaymentInvoiceService service;
    private final WebhookService webhookService;

    public PaymentInvoiceController(PaymentInvoiceService service, WebhookService webhookService) {
        this.service = service;
        this.webhookService = webhookService;
    }

    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<PaymentResponseDTO> result = service.getPaymentsByDateRange(from, to)
                .stream().map(PaymentResponseDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoices(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<InvoiceResponseDTO> result = service.getInvoicesByDateRange(from, to)
                .stream().map(InvoiceResponseDTO::from).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/payments/{paymentId}/refund")
    public ResponseEntity<PaymentResponseDTO> markRefund(@PathVariable Long paymentId) {
        return ResponseEntity.ok(PaymentResponseDTO.from(service.recordRefund(paymentId)));
    }

    @GetMapping("/bookings/{referenceId}/payment")
    public ResponseEntity<PaymentResponseDTO> getPaymentByBooking(@PathVariable String referenceId) {
        return ResponseEntity.ok(PaymentResponseDTO.from(service.getPaymentByBookingReference(referenceId)));
    }

    @GetMapping("/bookings/{referenceId}/invoice")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceByBooking(@PathVariable String referenceId) {
        return ResponseEntity.ok(InvoiceResponseDTO.from(service.getInvoiceByBookingReference(referenceId)));
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncPayments() {
        int count = webhookService.syncMissingPayments();
        return ResponseEntity.ok("Synced " + count + " webhook events.");
    }
}