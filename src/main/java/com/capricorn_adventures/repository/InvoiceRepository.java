package com.capricorn_adventures.repository;


import com.capricorn_adventures.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByBookingId(Long bookingId);
    List<Invoice> findByIssuedAtBetween(LocalDateTime from, LocalDateTime to);
    boolean existsByInvoiceNumber(String invoiceNumber);
}
