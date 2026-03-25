package com.capricorn_adventures.dto;

public class AdventureBookingValidationResponseDTO {
    private boolean allowed;
    private String message;

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
