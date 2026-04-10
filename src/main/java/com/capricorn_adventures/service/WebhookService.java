package com.capricorn_adventures.service;

import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.PaymentWebhookEvent;
import com.capricorn_adventures.exception.WebhookSignatureException;
import com.capricorn_adventures.repository.PaymentWebhookEventRepository;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final PaymentWebhookEventRepository webhookRepo;
    private final BookingRepository bookingRepo;
    private final PaymentInvoiceService paymentInvoiceService;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    @Value("${payhere.skip-sig-check:false}")
    private boolean skipSigCheck;

    public WebhookService(PaymentWebhookEventRepository webhookRepo,
                          BookingRepository bookingRepo,
                          PaymentInvoiceService paymentInvoiceService) {
        this.webhookRepo = webhookRepo;
        this.bookingRepo = bookingRepo;
        this.paymentInvoiceService = paymentInvoiceService;
    }

    public void handleWebhook(Map<String, String> params) {
        String orderId    = params.get("order_id");
        String amount     = params.get("payhere_amount");
        String currency   = params.get("payhere_currency");
        String statusCode = params.get("status_code");
        String md5sig     = params.get("md5sig");
        String paymentId  = params.get("payment_id"); // idempotency key

        // 1. Verify signature
        if (!skipSigCheck && !isValidSignature(orderId, amount, currency, statusCode, md5sig)) {
            throw new WebhookSignatureException("Invalid PayHere webhook signature");
        }

        // 2. Idempotency check — allow same paymentId if event type is different (state transition)
        String eventType = "PAYHERE_STATUS_" + statusCode;
        if (webhookRepo.findByEventId(paymentId).stream()
                .anyMatch(e -> e.getEventType().equals(eventType))) {
            log.info("Duplicate PayHere webhook ignored: {} with type {}", paymentId, eventType);
            return;
        }

        // 3. Persist webhook event
        PaymentWebhookEvent record = new PaymentWebhookEvent();
        record.setEventId(paymentId);
        record.setEventType("PAYHERE_STATUS_" + statusCode);
        record.setPayload(params.toString());
        record.setReceivedAt(LocalDateTime.now());
        record.setStatus("PROCESSING");
        webhookRepo.save(record);

        try {
            processStatusUpdate(params, record, statusCode, orderId, amount, currency, paymentId);
            record.setStatus("PROCESSED");
        } catch (Exception ex) {
            record.setStatus("FAILED");
            log.error("PayHere webhook processing failed for paymentId={}", paymentId, ex);
            throw ex;
        } finally {
            record.setProcessedAt(LocalDateTime.now());
            webhookRepo.save(record);
        }
    }

    private void processStatusUpdate(Map<String, String> params, PaymentWebhookEvent record, String statusCode,
                                     String orderId, String amount, String currency, String paymentId) {
        // 4. Handle by PayHere status code
        // 2 = Success, 0 = Pending, -1 = Canceled, -2 = Failed, -3 = Chargedback
        switch (statusCode) {
            case "2"        -> handlePaymentSuccess(orderId, amount, currency, paymentId);
            case "0", "1"   -> handlePaymentPending(orderId, amount, currency, paymentId);
            case "-3"       -> handleChargeback(orderId, paymentId, amount, currency);
            case "-1", "-2" -> handlePaymentFailed(orderId, paymentId, amount, currency);
            default         -> log.warn("Unhandled PayHere status code: {}", statusCode);
        }
    }

    // Recover missed payments by re-scanning all confirmed webhook events
    public int syncMissingPayments() {
        List<PaymentWebhookEvent> events = webhookRepo.findAll();
        int count = 0;
        for (PaymentWebhookEvent event : events) {
            // Very basic parse of the {key=val, ...} string if we need to re-process
            // But usually we can just look at the event type we stored
            if (event.getEventType().startsWith("PAYHERE_STATUS_")) {
                String statusCode = event.getEventType().replace("PAYHERE_STATUS_", "");
                // We'd need to extract orderId, amount, currency from the payload string
                // Since the format is simple, we can try to extract the main bits
                Map<String, String> params = parsePayload(event.getPayload());
                try {
                    processStatusUpdate(params, event, statusCode,
                            params.get("order_id"), params.get("payhere_amount"),
                            params.get("payhere_currency"), event.getEventId());
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to sync event {}: {}", event.getEventId(), e.getMessage());
                }
            }
        }
        return count;
    }

    private Map<String, String> parsePayload(String payload) {
        // Payload is stored as "{key=val, key2=val2}"
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        if (payload == null || !payload.startsWith("{") || !payload.endsWith("}")) return map;
        String content = payload.substring(1, payload.length() - 1);
        String[] pairs = content.split(", ");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0], kv[1]);
        }
        return map;
    }

    // US-17 AC1 — store payment record + auto-generate invoice
    private void handlePaymentSuccess(String orderId, String amount,
                                      String currency, String paymentId) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentReference(paymentId);
        bookingRepo.save(booking);

        paymentInvoiceService.recordSuccessfulPayment(
                orderId,
                paymentId,
                new BigDecimal(amount),
                currency,
                "PAYHERE"
        );

        log.info("Booking {} confirmed, payment & invoice stored", orderId);
    }

    private void handlePaymentPending(String orderId, String amount,
                                      String currency, String paymentId) {
        paymentInvoiceService.recordPendingPayment(
                orderId,
                paymentId,
                new BigDecimal(amount),
                currency,
                "PAYHERE"
        );
        log.info("Booking {} payment pending/authorized, record stored", orderId);
    }

    // US-17 AC4 — store failure reason
    private void handlePaymentFailed(String orderId, String paymentId,
                                     String amount, String currency) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        paymentInvoiceService.recordFailedPayment(
                orderId,
                paymentId,
                new BigDecimal(amount),
                currency,
                "Payment failed or cancelled by user"
        );

        log.info("Booking {} payment failed/cancelled, failure recorded", orderId);
    }

//    // US-17 AC2 — chargeback updates payment record
//    private void handleChargeback(String orderId, String paymentId,
//                                  String amount, String currency) {
//        Booking booking = bookingRepo.findByReferenceId(orderId)
//                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
//        booking.setStatus(BookingStatus.CANCELLED);
//        bookingRepo.save(booking);
//
//        // Record as failed with chargeback reason
//        paymentInvoiceService.recordFailedPayment(
//                orderId,
//                paymentId,
//                new BigDecimal(amount),
//                currency,
//                "Chargeback initiated by cardholder"
//        );
//
//        log.info("Booking {} charged back, record updated", orderId);
//    }

    private void handleChargeback(String orderId, String paymentId,
                                  String amount, String currency) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);

        paymentInvoiceService.recordChargeback(
                orderId,
                paymentId,
                new BigDecimal(amount),
                currency
        );

        log.info("Booking {} charged back, record updated", orderId);
    }

    private boolean isValidSignature(String orderId, String amount,
                                     String currency, String statusCode,
                                     String receivedMd5) {
        try {
            String hashedSecret = md5(merchantSecret).toUpperCase();
            String sigString    = merchantId + orderId + amount + currency + statusCode + hashedSecret;
            String expectedMd5  = md5(sigString).toUpperCase();

            log.debug("EXPECTED MD5: {}", expectedMd5);
            log.debug("RECEIVED MD5: {}", receivedMd5);

            return expectedMd5.equals(receivedMd5);
        } catch (Exception e) {
            log.error("PayHere signature validation error", e);
            return false;
        }
    }

    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}