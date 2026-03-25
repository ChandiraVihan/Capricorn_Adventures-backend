package com.capricorn_adventures.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceId(String referenceId);

    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Modifying
    void deleteByReferenceId(String referenceId);

    List<Booking> findByUserOrderByCheckInDateDesc(User user);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status IN :statuses " +
           "AND (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)")
    boolean isRoomOccupied(@Param("roomId") Long roomId,
                           @Param("statuses") List<BookingStatus> statuses,
                           @Param("checkInDate") LocalDate checkInDate,
                           @Param("checkOutDate") LocalDate checkOutDate);
}
