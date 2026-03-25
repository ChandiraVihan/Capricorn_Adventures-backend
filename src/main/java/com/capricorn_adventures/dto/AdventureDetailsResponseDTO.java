package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdventureDetailsResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private String primaryImageUrl;
    private List<String> photos;
    private String location;
    private String difficultyLevel;
    private Integer minAge;
    private String itinerary;
    private List<String> inclusions;
    private boolean active;
    private boolean bookable;
    private String message;
    private List<ScheduleSlotDTO> scheduleSlots;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(String difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public List<String> getInclusions() {
        return inclusions;
    }

    public void setInclusions(List<String> inclusions) {
        this.inclusions = inclusions;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBookable() {
        return bookable;
    }

    public void setBookable(boolean bookable) {
        this.bookable = bookable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ScheduleSlotDTO> getScheduleSlots() {
        return scheduleSlots;
    }

    public void setScheduleSlots(List<ScheduleSlotDTO> scheduleSlots) {
        this.scheduleSlots = scheduleSlots;
    }

    public static class ScheduleSlotDTO {
        private Long scheduleId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer availableSlots;
        private String status;
        private boolean available;
        private boolean disabled;
        private String disabledReason;
        private boolean inSelectedRange;

        public Long getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(Long scheduleId) {
            this.scheduleId = scheduleId;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
        }

        public Integer getAvailableSlots() {
            return availableSlots;
        }

        public void setAvailableSlots(Integer availableSlots) {
            this.availableSlots = availableSlots;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public String getDisabledReason() {
            return disabledReason;
        }

        public void setDisabledReason(String disabledReason) {
            this.disabledReason = disabledReason;
        }

        public boolean isInSelectedRange() {
            return inSelectedRange;
        }

        public void setInSelectedRange(boolean inSelectedRange) {
            this.inSelectedRange = inSelectedRange;
        }
    }
}
