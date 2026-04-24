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
            return List.of();
        }
    }

    public String createReservation(String partnerVehicleId,
                                    String pickupLocation,
                                    LocalDate pickupDate,
                                    LocalDate returnDate) {
        // Mock reservation ID since partner API is not implemented
        String mockReservationId = "RES-" + partnerVehicleId + "-" + System.currentTimeMillis();
        log.info("Mock reservation created: {}", mockReservationId);
        return mockReservationId;
    }

    public boolean releaseReservation(String partnerReservationId) {
        // Mock release since partner API is not implemented
        log.info("Mock reservation released: {}", partnerReservationId);
        return true;
    }

    public String getPartnerWebsiteUrl() { return partnerWebsiteUrl; }

    public String getPartnerName() { return partnerName; }

    private List<CarRentalVehicleDTO> callPartnerSearchApi(String pickupLocation,
                                                           LocalDate pickupDate,
                                                           LocalDate returnDate,
                                                           String vehicleCategory,
                                                           String preferredCurrency) {
        long rentalDays = ChronoUnit.DAYS.between(pickupDate, returnDate);
        if (rentalDays <= 0) rentalDays = 1;

        List<CarRentalVehicleDTO> mockVehicles = new ArrayList<>();

        CarRentalVehicleDTO v1 = new CarRentalVehicleDTO();
        v1.setPartnerVehicleId("V001");
        v1.setVehicleName("Toyota Corolla");
        v1.setVehicleCategory("SEDAN");
        v1.setVehicleImageUrl("https://i.postimg.cc/MH2zLdSZ/Rathu-olu-1-700x450.jpg");
        v1.setPartnerName(partnerName);
        v1.setPartnerWebsiteUrl(partnerWebsiteUrl);
        v1.setPricePerDay(convertCurrency(new BigDecimal("5000.00"), "LKR", preferredCurrency));
        v1.setCurrency(preferredCurrency);
        v1.setRentalDays((int) rentalDays);
        v1.setTotalPrice(v1.getPricePerDay().multiply(BigDecimal.valueOf(rentalDays)));
        mockVehicles.add(v1);

        CarRentalVehicleDTO v2 = new CarRentalVehicleDTO();
        v2.setPartnerVehicleId("V002");
        v2.setVehicleName("Honda Vezel");
        v2.setVehicleCategory("SUV");
        v2.setVehicleImageUrl("https://i.postimg.cc/MH2zLdSZ/Rathu-olu-1-700x450.jpg");
        v2.setPartnerName(partnerName);
        v2.setPartnerWebsiteUrl(partnerWebsiteUrl);
        v2.setPricePerDay(convertCurrency(new BigDecimal("7500.00"), "LKR", preferredCurrency));
        v2.setCurrency(preferredCurrency);
        v2.setRentalDays((int) rentalDays);
        v2.setTotalPrice(v2.getPricePerDay().multiply(BigDecimal.valueOf(rentalDays)));
        mockVehicles.add(v2);

        CarRentalVehicleDTO v3 = new CarRentalVehicleDTO();
        v3.setPartnerVehicleId("V003");
        v3.setVehicleName("Mercedes C-Class");
        v3.setVehicleCategory("LUXURY");
        v3.setVehicleImageUrl("https://i.postimg.cc/MH2zLdSZ/Rathu-olu-1-700x450.jpg");
        v3.setPartnerName(partnerName);
        v3.setPartnerWebsiteUrl(partnerWebsiteUrl);
        v3.setPricePerDay(convertCurrency(new BigDecimal("15000.00"), "LKR", preferredCurrency));
        v3.setCurrency(preferredCurrency);
        v3.setRentalDays((int) rentalDays);
        v3.setTotalPrice(v3.getPricePerDay().multiply(BigDecimal.valueOf(rentalDays)));
        mockVehicles.add(v3);

        return mockVehicles;
    }

    private BigDecimal convertCurrency(BigDecimal amount, String from, String to) {
        if (from.equalsIgnoreCase(to)) return amount;
        double lkrAmount = amount.doubleValue();
        double result = switch (to.toUpperCase()) {
            case "USD" -> lkrAmount / 310.0;
            case "EUR" -> lkrAmount / 335.0;
            default    -> lkrAmount;
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
