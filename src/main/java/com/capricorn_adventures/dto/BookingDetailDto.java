package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingDetailDto {
    private Long id;
    private String hotelName;
    private String hotelAddress;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private String roomType;
    private Integer guestsCount;
    private BigDecimal pricePaid;
    private BookingStatus status;
    private boolean isRefundable;
    private LocalDateTime cancellationDeadline;
    private String guestName;
}
