package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendCancellationEmail(Booking booking) {
        if (booking.getUser().getEmail() == null || booking.getUser().getEmail().isEmpty()) {
            logger.warn("User email is missing for user {}. Cannot send cancellation email.",
                    booking.getUser().getId());
            return;
        }

        logger.info("Sending cancellation email to {}...", booking.getUser().getEmail());
        logger.info("Booking Reference: {}", booking.getId());
        logger.info("Stay Dates: {} to {}", booking.getCheckInDate(), booking.getCheckOutDate());
        logger.info("Hotel Name: {}", booking.getHotel().getName());
        logger.info("Status: {}", booking.getStatus());
        logger.info("Email sent successfully.");
    }
}
