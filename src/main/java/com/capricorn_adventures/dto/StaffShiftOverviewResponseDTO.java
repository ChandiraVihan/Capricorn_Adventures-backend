package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class StaffShiftOverviewResponseDTO {

    private LocalDateTime generatedAt;
    private boolean autoRefreshEnabled;
    private int refreshIntervalSeconds;
    private List<DepartmentShiftDTO> departments;
    private OwnerShiftMetricsDTO ownerMetrics;

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public boolean isAutoRefreshEnabled() {
        return autoRefreshEnabled;
    }

    public void setAutoRefreshEnabled(boolean autoRefreshEnabled) {
        this.autoRefreshEnabled = autoRefreshEnabled;
    }

    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    public void setRefreshIntervalSeconds(int refreshIntervalSeconds) {
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public List<DepartmentShiftDTO> getDepartments() {
        return departments;
    }

    public void setDepartments(List<DepartmentShiftDTO> departments) {
        this.departments = departments;
    }

    public OwnerShiftMetricsDTO getOwnerMetrics() {
        return ownerMetrics;
    }

    public void setOwnerMetrics(OwnerShiftMetricsDTO ownerMetrics) {
        this.ownerMetrics = ownerMetrics;
    }

    public static class DepartmentShiftDTO {
        private String departmentCode;
        private String departmentName;
        private boolean understaffed;
        private String warning;
        private List<OnShiftStaffDTO> onShiftStaff;

        public String getDepartmentCode() {
            return departmentCode;
        }

        public void setDepartmentCode(String departmentCode) {
            this.departmentCode = departmentCode;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public boolean isUnderstaffed() {
            return understaffed;
        }

        public void setUnderstaffed(boolean understaffed) {
            this.understaffed = understaffed;
        }

        public String getWarning() {
            return warning;
        }

        public void setWarning(String warning) {
            this.warning = warning;
        }

        public List<OnShiftStaffDTO> getOnShiftStaff() {
            return onShiftStaff;
        }

        public void setOnShiftStaff(List<OnShiftStaffDTO> onShiftStaff) {
            this.onShiftStaff = onShiftStaff;
        }
    }

    public static class OnShiftStaffDTO {
        private Long shiftId;
        private UUID staffId;
        private String staffName;
        private LocalDateTime shiftStartAt;
        private String currentTaskAssignment;
        private LocalDateTime lastActivityAt;

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

        public LocalDateTime getShiftStartAt() {
            return shiftStartAt;
        }

        public void setShiftStartAt(LocalDateTime shiftStartAt) {
            this.shiftStartAt = shiftStartAt;
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
    }

    public static class OwnerShiftMetricsDTO {
        private LocalDate businessDate;
        private double totalLaborHours;
        private BigDecimal estimatedShiftCost;

        public LocalDate getBusinessDate() {
            return businessDate;
        }

        public void setBusinessDate(LocalDate businessDate) {
            this.businessDate = businessDate;
        }

        public double getTotalLaborHours() {
            return totalLaborHours;
        }

        public void setTotalLaborHours(double totalLaborHours) {
            this.totalLaborHours = totalLaborHours;
        }

        public BigDecimal getEstimatedShiftCost() {
            return estimatedShiftCost;
        }

        public void setEstimatedShiftCost(BigDecimal estimatedShiftCost) {
            this.estimatedShiftCost = estimatedShiftCost;
        }
    }
}
