package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.CarRentalBooking;
import com.capricorn_adventures.entity.CarRentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarRentalBookingRepository extends JpaRepository<CarRentalBooking, Long> {

    /**
     * Find the car rental add-on attached to a specific adventure checkout booking.
     * Used when adding/removing a car from the cart (AC3, AC5).
     */
    Optional<CarRentalBooking> findByAdventureCheckoutBookingId(Long adventureCheckoutBookingId);

    /**
     * Find a confirmed (but email-not-yet-sent) rental for email dispatch after payment (AC6).
     */
    @Query("""
        SELECT c FROM CarRentalBooking c
        WHERE c.adventureCheckoutBooking.id = :checkoutId
          AND c.status = :status
          AND c.rentalConfirmationEmailSent = false
    """)
    Optional<CarRentalBooking> findPendingConfirmationEmail(
            @Param("checkoutId") Long checkoutId,
            @Param("status") CarRentalStatus status
    );
}
