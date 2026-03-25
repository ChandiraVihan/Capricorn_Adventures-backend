package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateAdventureScheduleRequestDTO {

    @NotNull(message = "Adventure ID is required")
    private Long adventureId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Available slots are required")
    private Integer availableSlots;

    private String status = "AVAILABLE";

    // Getters and Setters
    public Long getAdventureId() { return adventureId; }
    public void setAdventureId(Long adventureId) { this.adventureId = adventureId; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
