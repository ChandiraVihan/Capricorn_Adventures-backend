package com.capricorn_adventures.dto;

import java.math.BigDecimal;

/**
 * Returned after adding/removing a car add-on (AC3, AC5).
 * Contains the car details plus the combined adventure+car total.
 */
public class CarRentalCartSummaryDTO {

    private Long carRentalBookingId;
    private String status;

    // Vehicle details
    private String vehicleName;
    private String vehicleCategory;
    private String vehicleImageUrl;
    private String partnerName;
    private String partnerWebsiteUrl;

    // Dates
    private String pickupDate;
    private String returnDate;
    private String pickupLocation;
    private int rentalDays;

    // Pricing (AC7 – currency matches user preference)
    private BigDecimal pricePerDay;
    private BigDecimal carRentalTotal;
    private String currency;

    // Combined total (adventure + car rental) (AC3)
    private BigDecimal adventureTotal;
    private BigDecimal combinedTotal;

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getCarRentalBookingId() { return carRentalBookingId; }
    public void setCarRentalBookingId(Long carRentalBookingId) { this.carRentalBookingId = carRentalBookingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getVehicleImageUrl() { return vehicleImageUrl; }
    public void setVehicleImageUrl(String vehicleImageUrl) { this.vehicleImageUrl = vehicleImageUrl; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerWebsiteUrl() { return partnerWebsiteUrl; }
    public void setPartnerWebsiteUrl(String partnerWebsiteUrl) { this.partnerWebsiteUrl = partnerWebsiteUrl; }

    public String getPickupDate() { return pickupDate; }
    public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }

    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public BigDecimal getCarRentalTotal() { return carRentalTotal; }
    public void setCarRentalTotal(BigDecimal carRentalTotal) { this.carRentalTotal = carRentalTotal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAdventureTotal() { return adventureTotal; }
    public void setAdventureTotal(BigDecimal adventureTotal) { this.adventureTotal = adventureTotal; }

    public BigDecimal getCombinedTotal() { return combinedTotal; }
    public void setCombinedTotal(BigDecimal combinedTotal) { this.combinedTotal = combinedTotal; }
}
