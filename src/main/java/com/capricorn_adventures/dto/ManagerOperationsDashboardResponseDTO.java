package com.capricorn_adventures.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ManagerOperationsDashboardResponseDTO {

    private LocalDate businessDate;
    private LocalDateTime generatedAt;
    private boolean autoRefreshEnabled;
    private int refreshIntervalSeconds;
    private List<TourSlotDTO> todayTours;
    private List<OccupancyDTO> weeklyOccupancy;

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

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

    public List<TourSlotDTO> getTodayTours() {
        return todayTours;
    }

    public void setTodayTours(List<TourSlotDTO> todayTours) {
        this.todayTours = todayTours;
    }

    public List<OccupancyDTO> getWeeklyOccupancy() {
        return weeklyOccupancy;
    }

    public void setWeeklyOccupancy(List<OccupancyDTO> weeklyOccupancy) {
        this.weeklyOccupancy = weeklyOccupancy;
    }

    public static class TourSlotDTO {
        private Long scheduleId;
        private Long adventureId;
        private String adventureName;
        private LocalDateTime startDateTime;
        private LocalDateTime endDateTime;
        private String status;
        private String assignedGuideName;
        private boolean guideAssigned;
        private boolean guideAssignmentRequired;
        private String quickActionLabel;
        private Integer checkedInCustomerCount;
        private Integer availableSlots;
        private Integer totalCapacity;
        private List<IssueDTO> issues;

        public Long getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
        }

        public Long getAdventureId() {
            return adventureId;
        }

        public void setAdventureId(Long adventureId) {
            this.adventureId = adventureId;
        }

        public String getAdventureName() {
            return adventureName;
        }

        public void setAdventureName(String adventureName) {
            this.adventureName = adventureName;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAssignedGuideName() {
            return assignedGuideName;
        }

        public void setAssignedGuideName(String assignedGuideName) {
            this.assignedGuideName = assignedGuideName;
        }

        public boolean isGuideAssigned() {
            return guideAssigned;
        }

        public void setGuideAssigned(boolean guideAssigned) {
            this.guideAssigned = guideAssigned;
        }

        public boolean isGuideAssignmentRequired() {
            return guideAssignmentRequired;
        }

        public void setGuideAssignmentRequired(boolean guideAssignmentRequired) {
            this.guideAssignmentRequired = guideAssignmentRequired;
        }

        public String getQuickActionLabel() {
            return quickActionLabel;
        }

        public void setQuickActionLabel(String quickActionLabel) {
            this.quickActionLabel = quickActionLabel;
        }

        public Integer getCheckedInCustomerCount() {
            return checkedInCustomerCount;
        }

        public void setCheckedInCustomerCount(Integer checkedInCustomerCount) {
            this.checkedInCustomerCount = checkedInCustomerCount;
        }

        public Integer getAvailableSlots() {
            return availableSlots;
        }

        public void setAvailableSlots(Integer availableSlots) {
            this.availableSlots = availableSlots;
        }

        public Integer getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(Integer totalCapacity) {
            this.totalCapacity = totalCapacity;
        }

        public List<IssueDTO> getIssues() {
            return issues;
        }

        public void setIssues(List<IssueDTO> issues) {
            this.issues = issues;
        }
    }

    public static class IssueDTO {
        private Long alertId;
        private Long scheduleId;
        private String type;
        private String priority;
        private String title;
        private String message;
        private LocalDateTime raisedAt;

        public Long getAlertId() {
            return alertId;
        }

        public void setAlertId(Long alertId) {
            this.alertId = alertId;
        }

        public Long getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getRaisedAt() {
            return raisedAt;
        }

        public void setRaisedAt(LocalDateTime raisedAt) {
            this.raisedAt = raisedAt;
        }
    }

    public static class OccupancyDTO {
        private LocalDate date;
        private String dayLabel;
        private Integer bookedCapacity;
        private Integer availableCapacity;
        private Integer totalCapacity;

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getDayLabel() {
            return dayLabel;
        }

        public void setDayLabel(String dayLabel) {
            this.dayLabel = dayLabel;
        }

        public Integer getBookedCapacity() {
            return bookedCapacity;
        }

        public void setBookedCapacity(Integer bookedCapacity) {
            this.bookedCapacity = bookedCapacity;
        }

        public Integer getAvailableCapacity() {
            return availableCapacity;
        }

        public void setAvailableCapacity(Integer availableCapacity) {
            this.availableCapacity = availableCapacity;
        }

        public Integer getTotalCapacity() {
            return totalCapacity;
        }

        public void setTotalCapacity(Integer totalCapacity) {
            this.totalCapacity = totalCapacity;
        }
    }
}