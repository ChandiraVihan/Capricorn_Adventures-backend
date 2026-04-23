package com.capricorn_adventures.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RoomServiceOrderCreateRequestDTO {

    @NotNull
    @Min(1)
    private Integer roomNumber;

    @NotNull
    @Min(0)
    private Integer floorNumber;

    @NotEmpty
    private List<String> itemsOrdered;

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public List<String> getItemsOrdered() {
        return itemsOrdered;
    }

    public void setItemsOrdered(List<String> itemsOrdered) {
        this.itemsOrdered = itemsOrdered;
    }
}
