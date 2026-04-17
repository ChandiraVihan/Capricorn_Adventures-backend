package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

        @Query("""
            select b
            from AdventureCheckoutBooking b
            join fetch b.adventure a
            join fetch b.schedule s
            where b.user.id = :userId
            order by b.createdAt desc
            """)
        List<AdventureCheckoutBooking> findByUserIdWithDetailsOrderByCreatedAtDesc(@Param("userId") java.util.UUID userId);

        @Query("""
            select b
            from AdventureCheckoutBooking b
            join fetch b.adventure a
            join fetch b.schedule s
            where b.id = :bookingId and b.user.id = :userId
            """)
        Optional<AdventureCheckoutBooking> findByIdAndUserIdWithDetails(@Param("bookingId") Long bookingId,
                                         @Param("userId") java.util.UUID userId);

        @Query("""
            select b
            from AdventureCheckoutBooking b
            join fetch b.adventure a
            join fetch b.schedule s
            where b.id = :bookingId and b.user.id = :userId
            """)
        Optional<AdventureCheckoutBooking> findByIdAndUserIdForUpdateWithDetails(@Param("bookingId") Long bookingId,
                                              @Param("userId") java.util.UUID userId);

    Optional<AdventureCheckoutBooking> findByBookingReference(String bookingReference);

    List<AdventureCheckoutBooking> findByCreatedAtBetweenAndStatusIn(LocalDateTime from,
                                                                     LocalDateTime to,
                                                                     List<AdventureCheckoutStatus> statuses);
    
    @Modifying
    @Transactional
    @Query("delete from AdventureCheckoutBooking b where b.adventure.id = :adventureId")
    void deleteByAdventureId(@Param("adventureId") Long adventureId);
}

