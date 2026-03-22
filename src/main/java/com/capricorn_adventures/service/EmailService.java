package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.Booking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    
    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmation(Booking booking) {
        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            String principalName = "Unknown";
            try {
                principalName = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            } catch (Exception ignored) {}
            log.warn("Cannot send booking confirmation: No user email associated with booking {}. Auth Name: {}", 
                booking.getId(), principalName);
            return;
        }

        String to = booking.getUser().getEmail();
        String firstName = booking.getUser().getFirstName();
        String referenceId = booking.getReferenceId();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Booking Confirmation - " + referenceId);
        message.setText(
            "Hi " + firstName + ",\n\n" +
            "Your booking at Capricorn Adventures has been confirmed!\n\n" +
            "Booking Details:\n" +
            "- Reference Number: " + referenceId + "\n" +
            "- Room: " + booking.getRoom().getName() + "\n" +
            "- Check-in: " + booking.getCheckInDate() + "\n" +
            "- Check-out: " + booking.getCheckOutDate() + "\n" +
            "- Total Price: LKR " + booking.getTotalPrice() + "\n\n" +
            "You can use this reference number to track your booking on our website.\n\n" +
            "Thank you for choosing Capricorn Adventures!\n\n" +
            "— Capricorn Adventures Team"
        );

        try {
            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to: {}", to, e);
        }
    }
}
