package com.capricorn_adventures.dto;

import java.time.LocalDateTime;

public class RoomServiceOrderEventDTO {

    private String eventType;
    private String message;
    private LocalDateTime occurredAt;
    private RoomServiceOrderCardDTO order;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public RoomServiceOrderCardDTO getOrder() {
        return order;
    }

    public void setOrder(RoomServiceOrderCardDTO order) {
        this.order = order;
    }
}
