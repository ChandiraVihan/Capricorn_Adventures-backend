package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class StaffShiftResponseDTO {

    private Long shiftId;
    private UUID staffId;
    private String staffName;
    private String department;
    private LocalDateTime shiftStartAt;
    private LocalDateTime shiftEndAt;
    private String currentTaskAssignment;
    private LocalDateTime lastActivityAt;
    private BigDecimal hourlyRate;

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public UUID getStaffId() {
        return staffId;
    }

    public void setStaffId(UUID staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
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