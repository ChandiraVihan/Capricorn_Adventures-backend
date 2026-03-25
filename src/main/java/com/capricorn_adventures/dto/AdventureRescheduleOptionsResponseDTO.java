package com.capricorn_adventures.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdventureRescheduleOptionsResponseDTO {
    private boolean allowed;
    private String message;
    private Long currentScheduleId;
    private List<ScheduleOptionDTO> availableSlots;

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

    public Long getCurrentScheduleId() {
        return currentScheduleId;
    }

    public void setCurrentScheduleId(Long currentScheduleId) {
        this.currentScheduleId = currentScheduleId;
    }

    public List<ScheduleOptionDTO> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<ScheduleOptionDTO> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public static class ScheduleOptionDTO {
        private Long scheduleId;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private Integer availableSlots;

        public Long getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
        }

        public LocalDateTime getStartDateTime() {
            return startDateTime;
        }

        public void setStartDateTime(LocalDateTime startDateTime) {
            this.startDateTime = startDateTime;
        }

        public LocalDateTime getEndDateTime() {
            return endDateTime;
        }

        public void setEndDateTime(LocalDateTime endDateTime) {
            this.endDateTime = endDateTime;
        }

        public Integer getAvailableSlots() {
            return availableSlots;
        }

        public void setAvailableSlots(Integer availableSlots) {
            this.availableSlots = availableSlots;
        }
    }
}
