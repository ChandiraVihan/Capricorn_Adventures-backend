package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotNull;

public class AdventureRescheduleRequestDTO {
    @NotNull(message = "newScheduleId is required")
    private Long newScheduleId;

    public Long getNewScheduleId() {
        return newScheduleId;
    }

    public void setNewScheduleId(Long newScheduleId) {
        this.newScheduleId = newScheduleId;
    }
}
