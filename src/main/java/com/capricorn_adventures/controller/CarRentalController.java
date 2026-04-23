package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.CarRentalAddToCartRequestDTO;
import com.capricorn_adventures.dto.CarRentalCartSummaryDTO;
import com.capricorn_adventures.dto.CarRentalSearchResponseDTO;
import com.capricorn_adventures.service.CarRentalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller for the car rental add-on feature.
 *
 * Base path: /api/car-rental
 *
 * Endpoints
 * ─────────
 * GET  /search                        → AC1, AC2, AC4  search available vehicles
 * POST /cart                          → AC3            add selected car to checkout cart
 * GET  /cart/{checkoutId}             → AC3            get current cart summary
 * DELETE /cart/{checkoutId}           → AC5            remove car and release reservation
 */
@RestController
@RequestMapping("/api/car-rental")
@CrossOrigin(origins = "*")
public class CarRentalController {

    private final CarRentalService carRentalService;

    @Autowired
    public CarRentalController(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
    }

    // ── AC1 + AC2 + AC4 ───────────────────────────────────────────────────

    /**
     * Search for available rental cars near the adventure location.
     *
     * AC1 – pickupLocation is pre-filled by the frontend from adventure.location,
     *       but accepted as a query parameter so any client can call it directly.
     * AC2 – returns vehicles with pricePerDay filtered by vehicleCategory.
     * AC4 – if the partner API is unavailable, the response has apiAvailable=false
     *       and a fallbackPartnerUrl for the user to book directly.
     * AC7 – preferredCurrency causes prices to be returned in the user's currency.
     *
     * Example:
     *   GET /api/car-rental/search
     *     ?pickupLocation=Colombo
     *     &pickupDate=2025-11-01
     *     &returnDate=2025-11-05
     *     &vehicleCategory=SUV
     *     &preferredCurrency=USD
     */
    @GetMapping("/search")
    public ResponseEntity<CarRentalSearchResponseDTO> searchAvailableVehicles(
            @RequestParam String pickupLocation,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
            @RequestParam(required = false) String vehicleCategory,
            @RequestParam(defaultValue = "LKR") String preferredCurrency) {

        CarRentalSearchResponseDTO response = carRentalService.searchAvailableVehicles(
                pickupLocation, pickupDate, returnDate, vehicleCategory, preferredCurrency);

        return ResponseEntity.ok(response);
    }

    // ── AC3: Add car to cart ───────────────────────────────────────────────

    /**
     * Add the selected vehicle to the adventure checkout cart.
     *
     * AC3 – persists a RESERVED CarRentalBooking and returns the combined
     *       adventure + car total so the UI can update immediately.
     *
     * Request body: CarRentalAddToCartRequestDTO
     * Response:     CarRentalCartSummaryDTO (with combinedTotal)
     */
    @PostMapping("/cart")
    public ResponseEntity<CarRentalCartSummaryDTO> addCarToCart(
            @Valid @RequestBody CarRentalAddToCartRequestDTO request) {

        CarRentalCartSummaryDTO summary = carRentalService.addCarToCart(request);
        return ResponseEntity.ok(summary);
    }

    // ── AC3: Get current cart summary ──────────────────────────────────────

    /**
     * Retrieve the car rental add-on currently in the cart for a checkout session.
     * Returns 204 No Content if no car has been added yet.
     */
    @GetMapping("/cart/{checkoutId}")
    public ResponseEntity<CarRentalCartSummaryDTO> getCartSummary(
            @PathVariable Long checkoutId) {

        CarRentalCartSummaryDTO summary = carRentalService.getCartSummary(checkoutId);

        if (summary == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(summary);
    }

    // ── AC5: Remove car from cart ──────────────────────────────────────────

    /**
     * Remove the car add-on from the cart.
     *
     * AC5 – releases the partner reservation and returns the recalculated
     *       total (adventure only, car rental is excluded).
     */
    @DeleteMapping("/cart/{checkoutId}")
    public ResponseEntity<CarRentalCartSummaryDTO> removeCarFromCart(
            @PathVariable Long checkoutId) {

        CarRentalCartSummaryDTO summary = carRentalService.removeCarFromCart(checkoutId);
        return ResponseEntity.ok(summary);
    }
}
