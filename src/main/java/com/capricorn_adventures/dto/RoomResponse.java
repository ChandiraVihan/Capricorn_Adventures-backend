package com.capricorn_adventures.dto;

import java.math.BigDecimal;

public class RoomResponse {

    private String roomType;
    private String details;
    private BigDecimal pricePerNight;

    public RoomResponse() {
    }

    public RoomResponse(String roomType, String details, BigDecimal pricePerNight) {
        this.roomType = roomType;
        this.details = details;
        this.pricePerNight = pricePerNight;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public BigDecimal getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(BigDecimal pricePerNight) {
        this.pricePerNight = pricePerNight;
    }
}
