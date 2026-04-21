package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.RoomServiceOrderStatus;
import jakarta.validation.constraints.NotNull;

public class RoomServiceOrderStatusUpdateRequestDTO {

    @NotNull
    private RoomServiceOrderStatus status;

    public RoomServiceOrderStatus getStatus() {
        return status;
    }

    public void setStatus(RoomServiceOrderStatus status) {
        this.status = status;
    }
}
