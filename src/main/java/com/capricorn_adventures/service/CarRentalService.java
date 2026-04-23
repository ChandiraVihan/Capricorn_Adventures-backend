package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.*;

import java.time.LocalDate;

/**
 * Car rental add-on service – covers all 7 acceptance criteria.
 */
public interface CarRentalService {

    /**
     * AC1 + AC2 + AC4: Search available vehicles near the adventure location.
     * Returns a response that includes either vehicle listings or a graceful
     * fallback message with a partner website link.
     */
    CarRentalSearchResponseDTO searchAvailableVehicles(String pickupLocation,
                                                       LocalDate pickupDate,
                                                       LocalDate returnDate,
                                                       String vehicleCategory,
                                                       String preferredCurrency);

    /**
     * AC3: Add a selected car to the adventure checkout cart.
     * Calls the partner API to create a reservation, persists a
     * CarRentalBooking with status RESERVED, and returns the combined total.
     */
    CarRentalCartSummaryDTO addCarToCart(CarRentalAddToCartRequestDTO request);

    /**
     * AC5: Remove the car add-on from the cart.
     * Releases the partner reservation and recalculates the total.
     */
    CarRentalCartSummaryDTO removeCarFromCart(Long adventureCheckoutBookingId);

    /**
     * AC6: Called after payment completes successfully.
     * Marks the rental as CONFIRMED and sends a separate rental confirmation email.
     */
    void confirmRentalAfterPayment(Long adventureCheckoutBookingId);

    /**
     * Retrieve the current car add-on summary for a checkout session.
     * Returns null if no car add-on exists yet.
     */
    CarRentalCartSummaryDTO getCartSummary(Long adventureCheckoutBookingId);
}
