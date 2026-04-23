package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Stores a car rental add-on that has been added to an adventure checkout cart.
 * One AdventureCheckoutBooking can have at most one CarRentalBooking (ONE-TO-ONE).
 *
 * Lifecycle:
 *   RESERVED  → car held while user is still in checkout
 *   CONFIRMED → payment completed, reservation locked
 *   RELEASED  → user removed the car from cart (rental partner notified)
 *   CANCELLED → booking cancelled post-payment (refund handled separately)
 */
@Entity
@Table(name = "car_rental_bookings")
public class CarRentalBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The adventure checkout this add-on belongs to. */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adventure_checkout_booking_id", nullable = false, unique = true)
    private AdventureCheckoutBooking adventureCheckoutBooking;

    // ── Rental partner data ────────────────────────────────────────────────

    /** Opaque reservation ID returned by the rental partner API. */
    @Column(nullable = false, length = 100)
    private String partnerReservationId;

    /** Name of the rental partner (e.g. "PickMe", "EasyCar"). */
    @Column(nullable = false, length = 100)
    private String partnerName;

    /** Deep-link to the partner website for fallback display. */
    @Column(length = 500)
    private String partnerWebsiteUrl;

    // ── Vehicle details ────────────────────────────────────────────────────

    @Column(nullable = false, length = 100)
    private String vehicleName;

    /** E.g. ECONOMY, COMPACT, SUV, LUXURY */
    @Column(nullable = false, length = 50)
    private String vehicleCategory;

    @Column(length = 255)
    private String vehicleImageUrl;

    // ── Dates & location ───────────────────────────────────────────────────

    @Column(nullable = false)
    private LocalDate pickupDate;

    @Column(nullable = false)
    private LocalDate returnDate;

    /** Pre-filled from adventure.location */
    @Column(nullable = false, length = 255)
    private String pickupLocation;

    // ── Pricing ────────────────────────────────────────────────────────────

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Currency in which the price was returned by the rental API.
     * Stored so we can convert to the user's preferred currency at display time.
     */
    @Column(nullable = false, length = 3)
    private String currency;

    // ── Status ─────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarRentalStatus status;

    // ── Confirmation email tracking ────────────────────────────────────────

    @Column
    private boolean rentalConfirmationEmailSent = false;

    // ── Audit ──────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Getters / Setters ──────────────────────────────────────────────────

    public Long getId() { return id; }

    public AdventureCheckoutBooking getAdventureCheckoutBooking() { return adventureCheckoutBooking; }
    public void setAdventureCheckoutBooking(AdventureCheckoutBooking adventureCheckoutBooking) {
        this.adventureCheckoutBooking = adventureCheckoutBooking;
    }

    public String getPartnerReservationId() { return partnerReservationId; }
    public void setPartnerReservationId(String partnerReservationId) { this.partnerReservationId = partnerReservationId; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getPartnerWebsiteUrl() { return partnerWebsiteUrl; }
    public void setPartnerWebsiteUrl(String partnerWebsiteUrl) { this.partnerWebsiteUrl = partnerWebsiteUrl; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }

    public String getVehicleImageUrl() { return vehicleImageUrl; }
    public void setVehicleImageUrl(String vehicleImageUrl) { this.vehicleImageUrl = vehicleImageUrl; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public CarRentalStatus getStatus() { return status; }
    public void setStatus(CarRentalStatus status) { this.status = status; }

    public boolean isRentalConfirmationEmailSent() { return rentalConfirmationEmailSent; }
    public void setRentalConfirmationEmailSent(boolean rentalConfirmationEmailSent) {
        this.rentalConfirmationEmailSent = rentalConfirmationEmailSent;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
