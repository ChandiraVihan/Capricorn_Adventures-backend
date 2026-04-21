package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class RoomServiceOrderAssignmentRequestDTO {

    @NotNull
    private UUID staffId;

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }
}
