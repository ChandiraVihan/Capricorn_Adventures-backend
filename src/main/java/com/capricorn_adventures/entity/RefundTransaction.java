package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "refund_transactions")
public class RefundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking roomBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adventure_booking_id")
    private AdventureCheckoutBooking adventureBooking;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(length = 100)
    private String paymentReference;

    @Column(length = 500)
    private String failureReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum RefundType { FULL, PARTIAL }
    public enum RefundStatus { PENDING, SUCCESS, FAILED }

    public RefundTransaction() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Booking getRoomBooking() { return roomBooking; }
    public void setRoomBooking(Booking roomBooking) { this.roomBooking = roomBooking; }

    public AdventureCheckoutBooking getAdventureBooking() { return adventureBooking; }
    public void setAdventureBooking(AdventureCheckoutBooking adventureBooking) { this.adventureBooking = adventureBooking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public RefundType getType() { return type; }
    public void setType(RefundType type) { this.type = type; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isRoomRefund() { return roomBooking != null; }
    public boolean isAdventureRefund() { return adventureBooking != null; }
}
