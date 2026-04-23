package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.PaymentNotifyRequest;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.repository.BookingRepository;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.util.PayHereUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final BookingRepository bookingRepository;
    private final AdventureCheckoutBookingRepository adventureBookingRepository;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

    public PaymentController(BookingRepository bookingRepository,
                             AdventureCheckoutBookingRepository adventureBookingRepository) {
        this.bookingRepository = bookingRepository;
        this.adventureBookingRepository = adventureBookingRepository;
    }

    @GetMapping("/generate-hash")
    public ResponseEntity<String> generateHash(
            @RequestParam String orderId,
            @RequestParam double amount,
            @RequestParam String currency) {

        // Format: merchant_id + order_id + amount + currency + md5(merchant_secret)
        String amountFormatted = String.format("%.2f", amount);
        String secretHash = PayHereUtils.getMd5(merchantSecret);

        String rawString = merchantId + orderId + amountFormatted + currency + secretHash;
        String finalHash = PayHereUtils.getMd5(rawString);

        return ResponseEntity.ok(finalHash);
    }

    @PostMapping("/notify")
    public ResponseEntity<Void> handlePaymentNotification(@ModelAttribute PaymentNotifyRequest request) {
        String secretHash = PayHereUtils.getMd5(merchantSecret);
        String rawString = request.getMerchant_id() +
                request.getOrder_id() +
                request.getPayhere_amount() +
                request.getPayhere_currency() +
                request.getStatus_code() +
                secretHash;

        String expectedHash = PayHereUtils.getMd5(rawString);

        if (!expectedHash.equalsIgnoreCase(request.getMd5sig())) {
            log.warn("Invalid PayHere signature for order: {}", request.getOrder_id());
            return ResponseEntity.badRequest().build();
        }

        // status_code "2" = payment successful in PayHere
        if ("2".equals(request.getStatus_code())) {
            String orderId = request.getOrder_id();
            String paymentId = request.getPayment_id();

            log.info("Payment successful - order: {}, paymentId: {}", orderId, paymentId);

            // Try room booking first (referenceId = orderId)
            bookingRepository.findByReferenceId(orderId).ifPresentOrElse(
                booking -> {
                    booking.setPaymentReference(paymentId);
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);
                    log.info("Saved PayHere payment_id {} on room booking {}", paymentId, orderId);
                },
                () -> {
                    // Try adventure booking (bookingReference = orderId)
                    adventureBookingRepository.findByBookingReference(orderId).ifPresent(ab -> {
                        ab.setPaymentReference(paymentId);
                        adventureBookingRepository.save(ab);
                        log.info("Saved PayHere payment_id {} on adventure booking {}", paymentId, orderId);
                    });
                }
            );
        } else {
            log.info("Payment not successful for order: {} - status: {}", request.getOrder_id(), request.getStatus_code());
        }

        return ResponseEntity.ok().build();
    }
}
