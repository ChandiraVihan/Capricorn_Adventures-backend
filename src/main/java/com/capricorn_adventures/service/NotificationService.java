package com.capricorn_adventures.service;

import com.capricorn_adventures.entity.Booking;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.RoomServiceOrder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendRefundConfirmation(Booking booking) {
        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            log.warn("Cannot send refund confirmation: No user email for booking {}", booking.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(booking.getUser().getEmail());
        message.setSubject("Refund Confirmation - " + booking.getReferenceId());
        message.setText(
            "Hi " + booking.getUser().getFirstName() + ",\n\n" +
            "A refund has been processed for your booking at Capricorn Adventures.\n\n" +
            "Details:\n" +
            "- Reference: " + booking.getReferenceId() + "\n" +
            "- Refund Amount: LKR " + booking.getRefundedAmount() + "\n" +
            "- Status: " + booking.getStatus() + "\n\n" +
            "Thank you,\n" +
            "Capricorn Adventures Team"
        );

        try {
            mailSender.send(message);
            log.info("Refund confirmation sent to: {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send refund email", e);
        }
    }

    public void sendRefundConfirmation(AdventureCheckoutBooking booking) {
        if (booking.getUser() == null || booking.getUser().getEmail() == null) {
            log.warn("Cannot send refund confirmation: No user email for adventure booking {}", booking.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(booking.getUser().getEmail());
        message.setSubject("Adventure Refund Confirmation - " + booking.getBookingReference());
        message.setText(
            "Hi " + booking.getUser().getFirstName() + ",\n\n" +
            "A refund has been processed for your adventure booking: " + booking.getAdventure().getName() + ".\n\n" +
            "Details:\n" +
            "- Reference: " + booking.getBookingReference() + "\n" +
            "- Refund Amount: LKR " + booking.getRefundedAmount() + "\n" +
            "- Status: " + booking.getStatus() + "\n\n" +
            "Thank you,\n" +
            "Capricorn Adventures Team"
        );

        try {
            mailSender.send(message);
            log.info("Adventure refund confirmation sent to: {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send adventure refund email", e);
        }
    }

    public void sendRoomServiceStaleOrderAlert(List<String> managerEmails, RoomServiceOrder order) {
        if (managerEmails == null || managerEmails.isEmpty()) {
            log.warn("No manager emails configured for stale room service order {}", order.getId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(managerEmails.toArray(new String[0]));
        message.setSubject("Stale Room Service Order Alert - #" + order.getId());
        message.setText(
                "An active room service order has not been updated for more than 20 minutes.\n\n" +
                "Order ID: " + order.getId() + "\n" +
                "Room Number: " + order.getRoomNumber() + "\n" +
                "Current Status: " + order.getStatus() + "\n" +
                "Placed At: " + order.getPlacedAt() + "\n\n" +
                "Please review this order in the operations dashboard."
        );

        try {
            mailSender.send(message);
            log.info("Stale room service alert sent for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send stale room service alert for order {}", order.getId(), e);
        }
    }
}
