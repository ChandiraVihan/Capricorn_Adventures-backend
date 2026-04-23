package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.CarRentalVehicleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP client for the external car rental partner API.
 *
 * AC4 – if the API call throws any exception (timeout, 5xx, unreachable)
 * this service returns an empty Optional, and the caller falls back to the
 * partner website link.
 *
 * AC7 – currency conversion is applied here before returning results.
 *       In production, inject a real FX rate provider; the stub below
 *       uses a hardcoded LKR/USD rate as a placeholder.
 */
@Service
public class CarRentalPartnerClient {

    private static final Logger log = LoggerFactory.getLogger(CarRentalPartnerClient.class);

    private final RestTemplate restTemplate;

    @Value("${car.rental.partner.base-url:https://api.rentalpartner.example.com}")
    private String baseUrl;

    @Value("${car.rental.partner.api-key:changeme}")
    private String apiKey;

    @Value("${car.rental.partner.website-url:https://www.rentalpartner.example.com}")
    private String partnerWebsiteUrl;

    @Value("${car.rental.partner.name:RentalPartner}")
    private String partnerName;

    public CarRentalPartnerClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Search for available vehicles.
     *
     * @return list of vehicles, or empty list if the partner API is unavailable (AC4)
     */
    public List<CarRentalVehicleDTO> searchAvailableVehicles(String pickupLocation,
                                                             LocalDate pickupDate,
                                                             LocalDate returnDate,
                                                             String vehicleCategory,
                                                             String preferredCurrency) {
        try {
            return callPartnerSearchApi(pickupLocation, pickupDate, returnDate,
                    vehicleCategory, preferredCurrency);
        } catch (Exception ex) {
            log.warn("Car rental partner API unavailable: {}", ex.getMessage());
            return List.of(); // triggers fallback in service layer (AC4)
        }
    }

    /**
     * Reserve a vehicle at the partner.
     * Returns the partner's reservation ID, or null if the call fails.
     */
    public String createReservation(String partnerVehicleId,
                                    String pickupLocation,
                                    LocalDate pickupDate,
                                    LocalDate returnDate) {
        try {
            String url = baseUrl + "/reservations";

            HttpHeaders headers = buildHeaders();
            Map<String, Object> body = Map.of(
                    "vehicleId",      partnerVehicleId,
                    "pickupLocation", pickupLocation,
                    "pickupDate",     pickupDate.toString(),
                    "returnDate",     returnDate.toString()
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK
                    || response.getStatusCode() == HttpStatus.CREATED) {
                Object reservationId = response.getBody() != null
                        ? response.getBody().get("reservationId") : null;
                return reservationId != null ? reservationId.toString() : null;
            }
        } catch (Exception ex) {
            log.error("Failed to create rental reservation for vehicle {}: {}",
                    partnerVehicleId, ex.getMessage());
        }
        return null;
    }

    /**
     * Release/cancel a reservation at the partner (AC5).
     * Returns true if release was acknowledged.
     */
    public boolean releaseReservation(String partnerReservationId) {
        try {
            String url = baseUrl + "/reservations/" + partnerReservationId + "/release";
            HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.DELETE,
                    request, Void.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.error("Failed to release rental reservation {}: {}",
                    partnerReservationId, ex.getMessage());
            return false;
        }
    }

    /** Partner website URL – used in AC4 fallback response. */
    public String getPartnerWebsiteUrl() { return partnerWebsiteUrl; }

    public String getPartnerName() { return partnerName; }

    // ── Private helpers ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private List<CarRentalVehicleDTO> callPartnerSearchApi(String pickupLocation,
                                                           LocalDate pickupDate,
                                                           LocalDate returnDate,
                                                           String vehicleCategory,
                                                           String preferredCurrency) {
        String url = baseUrl + "/vehicles/available"
                + "?pickupLocation=" + pickupLocation
                + "&pickupDate=" + pickupDate
                + "&returnDate=" + returnDate
                + (vehicleCategory != null ? "&category=" + vehicleCategory : "");

        HttpEntity<Void> request = new HttpEntity<>(buildHeaders());
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET,
                request, List.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            return List.of();
        }

        long rentalDays = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (rentalDays <= 0) rentalDays = 1;

        List<CarRentalVehicleDTO> results = new ArrayList<>();
        for (Object item : response.getBody()) {
            Map<String, Object> raw = (Map<String, Object>) item;
            CarRentalVehicleDTO dto = mapVehicle(raw, rentalDays, preferredCurrency);
            results.add(dto);
        }
        return results;
    }

    private CarRentalVehicleDTO mapVehicle(Map<String, Object> raw,
                                           long rentalDays,
                                           String preferredCurrency) {
        CarRentalVehicleDTO dto = new CarRentalVehicleDTO();
        dto.setPartnerVehicleId(str(raw, "id"));
        dto.setVehicleName(str(raw, "name"));
        dto.setVehicleCategory(str(raw, "category"));
        dto.setVehicleImageUrl(str(raw, "imageUrl"));
        dto.setPartnerName(partnerName);
        dto.setPartnerWebsiteUrl(partnerWebsiteUrl);

        // The partner returns prices in its native currency (LKR assumed here)
        BigDecimal rawPricePerDay = new BigDecimal(str(raw, "pricePerDay"));
        BigDecimal convertedPricePerDay = convertCurrency(rawPricePerDay, "LKR", preferredCurrency);

        dto.setPricePerDay(convertedPricePerDay);
        dto.setCurrency(preferredCurrency);
        dto.setRentalDays((int) rentalDays);
        dto.setTotalPrice(convertedPricePerDay.multiply(BigDecimal.valueOf(rentalDays)));
        return dto;
    }

    /**
     * AC7 – currency conversion.
     * Replace this stub with a real FX service (e.g. Open Exchange Rates).
     */
    private BigDecimal convertCurrency(BigDecimal amount, String from, String to) {
        if (from.equalsIgnoreCase(to)) return amount;
        // Stub: 1 USD = 310 LKR, 1 EUR = 335 LKR
        double lkrAmount = amount.doubleValue();
        double result = switch (to.toUpperCase()) {
            case "USD" -> lkrAmount / 310.0;
            case "EUR" -> lkrAmount / 335.0;
            default    -> lkrAmount; // fallback: no conversion
        };
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String str(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }
}
