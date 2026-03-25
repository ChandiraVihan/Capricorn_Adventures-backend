package com.capricorn_adventures.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AdventureCheckoutStartRequestDTO {

    @NotNull(message = "adventureId is required")
    private Long adventureId;

    @NotNull(message = "scheduleId is required")
    private Long scheduleId;

    @NotNull(message = "participants is required")
    @Min(value = 1, message = "participants must be at least 1")
    private Integer participants;

    public Long getAdventureId() {
        return adventureId;
    }

    public void setAdventureId(Long adventureId) {
        this.adventureId = adventureId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }
}
