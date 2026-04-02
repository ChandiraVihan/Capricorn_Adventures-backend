package com.capricorn_adventures.repository;

import com.capricorn_adventures.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, UUID> {
    List<RefundTransaction> findByRoomBookingId(Long bookingId);
    List<RefundTransaction> findByAdventureBookingId(Long bookingId);
}
