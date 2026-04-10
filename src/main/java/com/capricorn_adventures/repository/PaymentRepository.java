package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.Payment;
import com.capricorn_adventures.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    Optional<Payment> findByBookingId(Long bookingId);
    List<Payment> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Payment> findByStatus(PaymentStatus status);
}
