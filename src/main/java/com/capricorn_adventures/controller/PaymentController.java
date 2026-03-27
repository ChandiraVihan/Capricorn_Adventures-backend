package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.PaymentNotifyRequest;
import com.capricorn_adventures.util.PayHereUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    @Value("${payhere.merchant.id}")
    private String merchantId;

    @Value("${payhere.merchant.secret}")
    private String merchantSecret;

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
        // 1. Verify the 'md5sig' sent by PayHere using your Secret
        // md5sig = MD5(merchant_id + order_id + payhere_amount + payhere_currency + status_code + MD5(merchant_secret).toUpperCase()).toUpperCase()

        String secretHash = PayHereUtils.getMd5(merchantSecret);
        String rawString = request.getMerchant_id() +
                request.getOrder_id() +
                request.getPayhere_amount() +
                request.getPayhere_currency() +
                request.getStatus_code() +
                secretHash;

        String expectedHash = PayHereUtils.getMd5(rawString);

        if (expectedHash.equalsIgnoreCase(request.getMd5sig())) {
            // 2. If valid, update your database: order.setStatus("PAID")
            // Note: status_code "2" means success in PayHere
            if ("2".equals(request.getStatus_code())) {
                System.out.println("Payment Successful for Order ID: " + request.getOrder_id());
                // TODO: Update database status to PAID
            } else {
                System.out.println("Payment Failed/Pending for Order ID: " + request.getOrder_id() + " Status: " + request.getStatus_code());
            }
            return ResponseEntity.ok().build();
        } else {
            System.out.println("Invalid Payment Notification Signature for Order ID: " + request.getOrder_id());
            return ResponseEntity.badRequest().build();
        }
    }
}
