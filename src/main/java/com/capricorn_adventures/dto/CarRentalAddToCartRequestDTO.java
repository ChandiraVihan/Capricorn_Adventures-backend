package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request body for "Add car to booking" (AC3).
 * The user selects a vehicle from the search results and POSTs this.
 */
public class CarRentalAddToCartRequestDTO {

    @NotNull
    private Long adventureCheckoutBookingId;

    @NotBlank
    private String partnerVehicleId;

    @NotBlank
    private String partnerName;

    @NotNull
    private LocalDate pickupDate;

    @NotNull
    private LocalDate returnDate;

    @NotBlank
    private String pickupLocation;

    /** ISO 4217 – the currency used in the search that produced this vehicle. */
    @NotBlank
    private String preferredCurrency;

    public Long getAdventureCheckoutBookingId() { return adventureCheckoutBookingId; }
    public void setAdventureCheckoutBookingId(Long adventureCheckoutBookingId) {
        this.adventureCheckoutBookingId = adventureCheckoutBookingId;
    }

    public String getPartnerVehicleId() { return partnerVehicleId; }
    public void setPartnerVehicleId(String partnerVehicleId) { this.partnerVehicleId = partnerVehicleId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getPreferredCurrency() { return preferredCurrency; }
    public void setPreferredCurrency(String preferredCurrency) { this.preferredCurrency = preferredCurrency; }
}
