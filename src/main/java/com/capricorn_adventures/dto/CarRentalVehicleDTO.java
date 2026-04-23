package com.capricorn_adventures.dto;

import java.math.BigDecimal;

/**
 * One vehicle returned by the rental partner search (AC2).
 * pricePerDay is already converted to the user's preferredCurrency (AC7).
 */
public class CarRentalVehicleDTO {

    private String partnerVehicleId;
    private String vehicleName;
    private String vehicleCategory;
    private String vehicleImageUrl;
    private String partnerName;
    private String partnerWebsiteUrl;

    /** Price converted to the user's preferred currency (AC7). */
    private BigDecimal pricePerDay;
    private String currency;

    /** Calculated from pricePerDay * rentalDays. */
    private BigDecimal totalPrice;
    private int rentalDays;

    public String getPartnerVehicleId() { return partnerVehicleId; }
    public void setPartnerVehicleId(String partnerVehicleId) { this.partnerVehicleId = partnerVehicleId; }

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

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }
}
