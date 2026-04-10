package com.capricorn_adventures.service;

import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.PaymentWebhookEvent;
import com.capricorn_adventures.exception.WebhookSignatureException;
import com.capricorn_adventures.repository.PaymentWebhookEventRepository;
import java.util.List;
import com.capricorn_adventures.util.PayHereUtils;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

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
        String statusCode = params.get("status_code");
        String paymentId  = params.get("payment_id"); 
        String orderId    = params.get("order_id");

        // 1. Ensure eventId is never null (fallback to orderId + status)
        String finalEventId = paymentId != null ? paymentId : orderId + "_" + statusCode + "_" + System.currentTimeMillis();

        // 2. Audit step: Save in a separate transaction so it's committed even if processing fails
        PaymentWebhookEvent record = saveInitialAudit(params, finalEventId, statusCode);

        // 3. Validate signature
        String amount     = params.get("payhere_amount");
        String currency   = params.get("payhere_currency");
        String md5Sig     = params.get("md5sig");

        String secretHash = PayHereUtils.getMd5(merchantSecret);
        String rawString = merchantId + orderId + amount + currency + statusCode + secretHash;
        String expectedHash = PayHereUtils.getMd5(rawString);

        if (!skipSigCheck && !expectedHash.equalsIgnoreCase(md5Sig)) {
            log.warn("Invalid PayHere signature for orderId={}. Expected={}, Got={}", orderId, expectedHash, md5Sig);
            updateEventStatus(record, "INVALID_SIGNATURE");
            return;
        }

        updateEventStatus(record, "PROCESSING");

        try {
            processStatusUpdate(params, record, statusCode, orderId, amount, currency, finalEventId);
            updateEventStatus(record, "PROCESSED");
        } catch (Exception ex) {
            updateEventStatus(record, "FAILED");
            log.error("PayHere webhook processing failed for eventId={}", finalEventId, ex);
            // We don't rethrow here to ensure the 200 OK is sent to PayHere, stopping retries
            // and keeping our "FAILED" status committed.
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentWebhookEvent saveInitialAudit(Map<String, String> params, String eventId, String statusCode) {
        PaymentWebhookEvent record = new PaymentWebhookEvent();
        record.setEventId(eventId);
        record.setEventType("PAYHERE_STATUS_" + statusCode);
        record.setPayload(params.toString());
        record.setStatus("RECEIVED");
        record.setReceivedAt(LocalDateTime.now());
        return webhookRepo.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateEventStatus(PaymentWebhookEvent record, String status) {
        record.setStatus(status);
        record.setProcessedAt(LocalDateTime.now());
        webhookRepo.save(record);
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