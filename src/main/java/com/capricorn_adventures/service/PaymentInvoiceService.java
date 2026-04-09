package com.capricorn_adventures.service;


import com.capricorn_adventures.entity.*;
import com.capricorn_adventures.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
@Transactional
public class PaymentInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(PaymentInvoiceService.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    private final PaymentRepository paymentRepo;
    private final InvoiceRepository invoiceRepo;
    private final BookingRepository bookingRepo;


    public PaymentInvoiceService(PaymentRepository paymentRepo,
                                 InvoiceRepository invoiceRepo,
                                 BookingRepository bookingRepo) {
        this.paymentRepo = paymentRepo;
        this.invoiceRepo = invoiceRepo;
        this.bookingRepo = bookingRepo;
    }

    @PersistenceContext
    private EntityManager entityManager;

    private String generateUniqueInvoiceNumber() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Long nextVal = (Long) entityManager
                .createNativeQuery("SELECT nextval('invoice_number_seq')")
                .getSingleResult();

        return String.format("INV-%s-%06d", datePrefix, nextVal);
    }

    // AC1 — Store payment record on successful payment
    public Payment recordSuccessfulPayment(String bookingReferenceId,
                                           String transactionId,
                                           BigDecimal amount,
                                           String currency,
                                           String gatewayMethod) {
        Booking booking = bookingRepo.findByReferenceId(bookingReferenceId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingReferenceId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(transactionId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setGatewayMethod(gatewayMethod);
        Payment saved = paymentRepo.save(payment);

        // Auto-generate invoice on successful payment
        generateInvoice(saved, booking);

        log.info("Payment recorded and invoice generated for booking {}", bookingReferenceId);
        return saved;
    }

    // AC4 — Store failed payment with failure reason
    public Payment recordFailedPayment(String bookingReferenceId,
                                       String transactionId,
                                       BigDecimal amount,
                                       String currency,
                                       String failureReason) {
        Booking booking = bookingRepo.findByReferenceId(bookingReferenceId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingReferenceId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(transactionId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        Payment saved = paymentRepo.save(payment);

        log.info("Failed payment recorded for booking {} — reason: {}", bookingReferenceId, failureReason);
        return saved;
    }

    // AC2 — Update payment record when refund is processed
    public Payment recordRefund(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found: " + paymentId));

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepo.save(payment);

        // Void the invoice
        invoiceRepo.findByBookingId(payment.getBooking().getId())
                .ifPresent(inv -> {
                    inv.setStatus(InvoiceStatus.VOID);
                    invoiceRepo.save(inv);
                });

        log.info("Payment {} marked as refunded", paymentId);
        return saved;
    }

    // AC3 — Get all payments for a date range (finance export)
    public List<Payment> getPaymentsByDateRange(LocalDateTime from, LocalDateTime to) {
        return paymentRepo.findByCreatedAtBetween(from, to);
    }

    public List<Invoice> getInvoicesByDateRange(LocalDateTime from, LocalDateTime to) {
        return invoiceRepo.findByIssuedAtBetween(from, to);
    }

    // AC5 — Generate invoice with unique invoice number
    private Invoice generateInvoice(Payment payment, Booking booking) {
        String invoiceNumber = generateUniqueInvoiceNumber();

        BigDecimal subtotal   = payment.getAmount();
        BigDecimal tax        = subtotal.multiply(TAX_RATE);
        BigDecimal total      = subtotal.add(tax);

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setPayment(payment);
        invoice.setBooking(booking);
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(tax);
        invoice.setTotalAmount(total);
        invoice.setStatus(InvoiceStatus.ISSUED);

        return invoiceRepo.save(invoice);
    }

//    // AC5 — Ensures uniqueness with retry
//    private String generateUniqueInvoiceNumber() {
//        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        String candidate;
//        do {
//            candidate = String.format("INV-%s-%06d", datePrefix, invoiceSequence.getAndIncrement());
//        } while (invoiceRepo.existsByInvoiceNumber(candidate));
//        return candidate;
//    }



    public Payment getPaymentByBookingReference(String referenceId) {
        Booking booking = bookingRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return paymentRepo.findByBookingId(booking.getId())
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking"));
    }

    public Invoice getInvoiceByBookingReference(String referenceId) {
        Booking booking = bookingRepo.findByReferenceId(referenceId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
        return invoiceRepo.findByBookingId(booking.getId())
                .orElseThrow(() -> new EntityNotFoundException("Invoice not found for booking"));
    }

    public Payment recordChargeback(String bookingReferenceId,
                                    String transactionId,
                                    BigDecimal amount,
                                    String currency) {
        Booking booking = bookingRepo.findByReferenceId(bookingReferenceId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingReferenceId));

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setTransactionId(transactionId);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setStatus(PaymentStatus.CHARGEBACK);
        payment.setFailureReason("Chargeback initiated by cardholder");
        Payment saved = paymentRepo.save(payment);

        // Void the invoice if exists
        invoiceRepo.findByBookingId(booking.getId())
                .ifPresent(inv -> {
                    inv.setStatus(InvoiceStatus.VOID);
                    invoiceRepo.save(inv);
                });

        log.info("Chargeback recorded for booking {}", bookingReferenceId);
        return saved;
    }
}
