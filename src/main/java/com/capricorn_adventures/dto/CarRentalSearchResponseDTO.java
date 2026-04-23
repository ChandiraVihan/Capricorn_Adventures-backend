package com.capricorn_adventures.dto;

import java.util.List;

/**
 * Response from the car rental search endpoint.
 *
 * AC4 (graceful fallback): if the rental API is unavailable,
 * {@code apiAvailable} is false and {@code fallbackPartnerUrl} is populated.
 * The frontend should render the fallback message + link when apiAvailable=false.
 */
public class CarRentalSearchResponseDTO {

    private boolean apiAvailable;

    /** Populated only when apiAvailable=false (AC4). */
    private String fallbackPartnerUrl;
    private String fallbackMessage;

    /** Populated when apiAvailable=true (AC2). */
    private List<CarRentalVehicleDTO> vehicles;

    private String pickupLocation;
    private String preferredCurrency;

    // ── Static factory helpers ─────────────────────────────────────────────

    public static CarRentalSearchResponseDTO success(List<CarRentalVehicleDTO> vehicles,
                                                     String pickupLocation,
                                                     String preferredCurrency) {
        CarRentalSearchResponseDTO dto = new CarRentalSearchResponseDTO();
        dto.apiAvailable = true;
        dto.vehicles = vehicles;
        dto.pickupLocation = pickupLocation;
        dto.preferredCurrency = preferredCurrency;
        return dto;
    }

    public static CarRentalSearchResponseDTO fallback(String partnerUrl) {
        CarRentalSearchResponseDTO dto = new CarRentalSearchResponseDTO();
        dto.apiAvailable = false;
        dto.fallbackPartnerUrl = partnerUrl;
        dto.fallbackMessage =
                "Car rental search is temporarily unavailable. " +
                        "You can still book directly with our partner.";
        return dto;
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public boolean isApiAvailable() { return apiAvailable; }
    public String getFallbackPartnerUrl() { return fallbackPartnerUrl; }
    public String getFallbackMessage() { return fallbackMessage; }
    public List<CarRentalVehicleDTO> getVehicles() { return vehicles; }
    public String getPickupLocation() { return pickupLocation; }
    public String getPreferredCurrency() { return preferredCurrency; }
}
