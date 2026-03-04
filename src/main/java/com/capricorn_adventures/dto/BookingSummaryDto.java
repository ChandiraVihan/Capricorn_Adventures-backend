package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.BookingStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingSummaryDto {
    private Long id;
    private String hotelName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private BookingStatus status;
}
