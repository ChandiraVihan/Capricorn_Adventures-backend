package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.BookingStatus;
import com.capricorn_adventures.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.maxOccupancy >= :guests " +
           "AND r.id NOT IN (SELECT b.room.id FROM Booking b " +
           "WHERE b.status IN :statuses " +
           "AND (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate))")
    List<Room> findAvailableRooms(@Param("guests") Integer guests,
                                  @Param("statuses") List<BookingStatus> statuses,
                                  @Param("checkInDate") LocalDate checkInDate,
                                  @Param("checkOutDate") LocalDate checkOutDate);
}
