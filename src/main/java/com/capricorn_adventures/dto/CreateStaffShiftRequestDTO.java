package com.capricorn_adventures.dto;

import com.capricorn_adventures.entity.StaffDepartment;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateStaffShiftRequestDTO {

    @NotNull(message = "Staff ID is required")
    private UUID staffId;

    @NotNull(message = "Department is required")
    private StaffDepartment department;

    @NotNull(message = "Shift start time is required")
    private LocalDateTime shiftStartAt;

    private LocalDateTime shiftEndAt;

    private String currentTaskAssignment;

    private LocalDateTime lastActivityAt;

    private BigDecimal hourlyRate;

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }

    public StaffDepartment getDepartment() {
        return department;
    }

    public void setDepartment(StaffDepartment department) {
        this.department = department;
    }

    public LocalDateTime getShiftStartAt() {
        return shiftStartAt;
    }

    public void setShiftStartAt(LocalDateTime shiftStartAt) {
        this.shiftStartAt = shiftStartAt;
    }

    public LocalDateTime getShiftEndAt() {
        return shiftEndAt;
    }

    public void setShiftEndAt(LocalDateTime shiftEndAt) {
        this.shiftEndAt = shiftEndAt;
    }

    public String getCurrentTaskAssignment() {
        return currentTaskAssignment;
    }

    public void setCurrentTaskAssignment(String currentTaskAssignment) {
        this.currentTaskAssignment = currentTaskAssignment;
    }

    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }
}