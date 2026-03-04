package com.capricorn_adventures.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;

    private String roomType;
    private Integer guestsCount;
    private BigDecimal pricePaid;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private boolean isRefundable;
    private LocalDateTime cancellationDeadline;
}
