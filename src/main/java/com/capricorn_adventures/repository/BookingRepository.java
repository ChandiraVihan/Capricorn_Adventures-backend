package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status IN :statuses " +
           "AND (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)")
    boolean isRoomOccupied(@Param("roomId") Long roomId,
                           @Param("statuses") List<BookingStatus> statuses,
                           @Param("checkInDate") LocalDate checkInDate,
                           @Param("checkOutDate") LocalDate checkOutDate);

    List<Booking> findAllByOrderByCheckInDateDesc();

    List<Booking> findByCheckInDateGreaterThanEqualAndCheckOutDateLessThanEqualOrderByCheckInDateDesc(
           LocalDate startDate,
           LocalDate endDate
    );
}
