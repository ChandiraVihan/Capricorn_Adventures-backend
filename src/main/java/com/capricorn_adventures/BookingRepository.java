package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceId(String referenceId);

    List<Booking> findByUserIdAndBookingDateBetween(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}