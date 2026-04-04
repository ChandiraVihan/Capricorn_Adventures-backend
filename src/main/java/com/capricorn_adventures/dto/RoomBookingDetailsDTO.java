package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RoomBookingDetailsDTO {
    private Long id;
    private String referenceId;
    private String roomName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private BigDecimal totalPrice;
    private String status;
    private boolean cancelAllowed;
    private String restrictionMessage;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }

    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isCancelAllowed() { return cancelAllowed; }
    public void setCancelAllowed(boolean cancelAllowed) { this.cancelAllowed = cancelAllowed; }

    public String getRestrictionMessage() { return restrictionMessage; }
    public void setRestrictionMessage(String restrictionMessage) { this.restrictionMessage = restrictionMessage; }
}
