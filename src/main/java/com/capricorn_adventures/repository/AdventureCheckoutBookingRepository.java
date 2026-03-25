package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdventureCheckoutBookingRepository extends JpaRepository<AdventureCheckoutBooking, Long> {

    @Query("""
            select b
            from AdventureCheckoutBooking b
            join fetch b.adventure a
            join fetch b.schedule s
            left join fetch b.user u
            where b.id = :checkoutId
            """)
    Optional<AdventureCheckoutBooking> findByIdWithDetails(@Param("checkoutId") Long checkoutId);

    Optional<AdventureCheckoutBooking> findByBookingReference(String bookingReference);
}
