package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdventureBookingDetailsDTO {
    private Long bookingId;
    private String bookingReference;
    private String adventureName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer participants;
    private BigDecimal totalPrice;
    private String status;
    private String providerInfo;
    private String meetingPoint;
    private List<String> whatToBring;
    private boolean rescheduleAllowed;
    private boolean cancelAllowed;
    private String restrictionMessage;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getAdventureName() {
        return adventureName;
    }

    public void setAdventureName(String adventureName) {
        this.adventureName = adventureName;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProviderInfo() {
        return providerInfo;
    }

    public void setProviderInfo(String providerInfo) {
        this.providerInfo = providerInfo;
    }

    public String getMeetingPoint() {
        return meetingPoint;
    }

    public void setMeetingPoint(String meetingPoint) {
        this.meetingPoint = meetingPoint;
    }

    public List<String> getWhatToBring() {
        return whatToBring;
    }

    public void setWhatToBring(List<String> whatToBring) {
        this.whatToBring = whatToBring;
    }

    public boolean isRescheduleAllowed() {
        return rescheduleAllowed;
    }

    public void setRescheduleAllowed(boolean rescheduleAllowed) {
        this.rescheduleAllowed = rescheduleAllowed;
    }

    public boolean isCancelAllowed() {
        return cancelAllowed;
    }

    public void setCancelAllowed(boolean cancelAllowed) {
        this.cancelAllowed = cancelAllowed;
    }

    public String getRestrictionMessage() {
        return restrictionMessage;
    }

    public void setRestrictionMessage(String restrictionMessage) {
        this.restrictionMessage = restrictionMessage;
    }
}
