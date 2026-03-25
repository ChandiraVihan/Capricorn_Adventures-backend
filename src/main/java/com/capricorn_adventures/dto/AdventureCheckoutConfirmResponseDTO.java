package com.capricorn_adventures.dto;

import java.util.List;

public class AdventureCheckoutConfirmResponseDTO {
    private boolean confirmed;
    private String bookingReference;
    private String message;
    private String meetingPoint;
    private List<String> whatToBring;
    private boolean retryAllowed;
    private String nextAction;

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public boolean isRetryAllowed() {
        return retryAllowed;
    }

    public void setRetryAllowed(boolean retryAllowed) {
        this.retryAllowed = retryAllowed;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }
}
