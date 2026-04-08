package com.capricorn_adventures.service;

import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.PaymentWebhookEvent;
import com.capricorn_adventures.exception.WebhookSignatureException;
import com.capricorn_adventures.repository.PaymentWebhookEventRepository;

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

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    public WebhookService(PaymentWebhookEventRepository webhookRepo,
                          BookingRepository bookingRepo) {
        this.webhookRepo = webhookRepo;
        this.bookingRepo = bookingRepo;
    }

    public void handleWebhook(Map<String, String> params) {
        String orderId     = params.get("order_id");
        String amount      = params.get("payhere_amount");
        String currency    = params.get("payhere_currency");
        String statusCode  = params.get("status_code");
        String md5sig      = params.get("md5sig");
        String paymentId   = params.get("payment_id"); // idempotency key

        // 1. Verify signature
        if (!isValidSignature(orderId, amount, currency, statusCode, md5sig)) {
            throw new WebhookSignatureException("Invalid PayHere webhook signature");
        }

        // 2. Idempotency check
        if (webhookRepo.findByEventId(paymentId).isPresent()) {
            log.info("Duplicate PayHere webhook ignored: {}", paymentId);
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
            // 4. Handle by PayHere status code
            // 2 = Success, 0 = Pending, -1 = Canceled, -2 = Failed, -3 = Chargedback
            switch (statusCode) {
                case "2"  -> handlePaymentSuccess(orderId, amount);
                case "-3" -> handleChargeback(orderId);
                case "-1", "-2" -> handlePaymentFailed(orderId);
                default   -> log.warn("Unhandled PayHere status code: {}", statusCode);
            }
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

    private void handlePaymentSuccess(String orderId, String amount) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentReference(orderId);
        bookingRepo.save(booking);
        log.info("Booking {} confirmed via PayHere", orderId);
    }

    private void handleChargeback(String orderId) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);
        log.info("Booking {} charged back", orderId);
    }

    private void handlePaymentFailed(String orderId) {
        Booking booking = bookingRepo.findByReferenceId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + orderId));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepo.save(booking);
        log.info("Booking {} payment failed/cancelled", orderId);
    }

    private boolean isValidSignature(String orderId, String amount,
                                     String currency, String statusCode,
                                     String receivedMd5) {
        try {
            // MD5 of merchant secret (uppercase)
            String hashedSecret = md5(merchantSecret).toUpperCase();

            // Build signature string
            String sigString = merchantId + orderId + amount + currency + statusCode + hashedSecret;

            // MD5 of full string (uppercase)
            String expectedMd5 = md5(sigString).toUpperCase();

    log.info("EXPECTED MD5: {}", expectedMd5);
        log.info("RECEIVED MD5: {}", receivedMd5);

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