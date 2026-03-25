package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdventureBrowseResponseDTO {
    private List<AdventureSummaryDTO> adventures;
    private boolean emptyState;
    private String message;
    private List<AdventureCategoryCardDTO> suggestions;
    private AppliedFilters appliedFilters;

    public List<AdventureSummaryDTO> getAdventures() {
        return adventures;
    }

    public void setAdventures(List<AdventureSummaryDTO> adventures) {
        this.adventures = adventures;
    }

    public boolean isEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<AdventureCategoryCardDTO> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<AdventureCategoryCardDTO> suggestions) {
        this.suggestions = suggestions;
    }

    public AppliedFilters getAppliedFilters() {
        return appliedFilters;
    }

    public void setAppliedFilters(AppliedFilters appliedFilters) {
        this.appliedFilters = appliedFilters;
    }

    public static class AppliedFilters {
        private Long categoryId;
        private String category;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer minDurationHours;
        private Integer maxDurationHours;

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(BigDecimal minPrice) {
            this.minPrice = minPrice;
        }

        public BigDecimal getMaxPrice() {
            return maxPrice;
        }

        public void setMaxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice;
        }

        public Integer getMinDurationHours() {
            return minDurationHours;
        }

        public void setMinDurationHours(Integer minDurationHours) {
            this.minDurationHours = minDurationHours;
        }

        public Integer getMaxDurationHours() {
            return maxDurationHours;
        }

        public void setMaxDurationHours(Integer maxDurationHours) {
            this.maxDurationHours = maxDurationHours;
        }
    }
}
