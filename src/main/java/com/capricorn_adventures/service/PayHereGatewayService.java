package com.capricorn_adventures.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

@Service
public class PayHereGatewayService {

    private static final Logger log = LoggerFactory.getLogger(PayHereGatewayService.class);

    private final RestTemplate restTemplate;

    @Value("${payhere.base.url}")
    private String baseUrl;

    @Value("${payhere.app.id}")
    private String appId;

    @Value("${payhere.app.secret}")
    private String appSecret;

    @Value("${payhere.merchant.id}")
    private String merchantId;

    public PayHereGatewayService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchAccessToken() {
        String url = baseUrl + "/oauth/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = appId + ":" + appSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("Failed to fetch PayHere access token", e);
        }
        return null;
    }

    public Map<String, Object> processRefund(String paymentId, BigDecimal amount, String description) {
        String token = fetchAccessToken();
        if (token == null) {
            throw new RuntimeException("Could not authenticate with PayHere");
        }

        String url = baseUrl + "/payment/refund";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);

        Map<String, Object> body = Map.of(
            "payment_id", paymentId,
            "amount", amount,
            "description", description
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (Map<String, Object>) response.getBody();
            }
        } catch (Exception e) {
            log.error("PayHere refund request failed for paymentId: {}", paymentId, e);
        }
        return null;
    }
}
