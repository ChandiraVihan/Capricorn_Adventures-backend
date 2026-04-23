package com.capricorn_adventures.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// ──────────────────────────────────────────────────────────────────────────────
// Request: search for available rental cars
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Acceptance criteria (AC1, AC2):
 *   – pickupLocation is pre-filled from adventure.location on the frontend,
 *     but the backend also accepts it explicitly so mobile/API clients work.
 *   – vehicleCategory filters results (optional: null = all categories).
 */
class CarRentalSearchRequestDTO {

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotNull(message = "pickupDate is required")
    @FutureOrPresent
    private LocalDate pickupDate;

    @NotNull(message = "returnDate is required")
    @FutureOrPresent
    private LocalDate returnDate;

    /** Optional: ECONOMY | COMPACT | SUV | LUXURY. Null = return all. */
    private String vehicleCategory;

    /**
     * ISO 4217 currency code requested by the user (AC7).
     * Defaults to "LKR" if not provided.
     */
    private String preferredCurrency = "LKR";

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getPreferredCurrency() { return preferredCurrency; }
    public void setPreferredCurrency(String preferredCurrency) { this.preferredCurrency = preferredCurrency; }
}
