package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdventureBrowseResponseDTO {
    private List<AdventureSummaryDTO> adventures;
    private boolean emptyState;
    private String message;
    private List<AdventureCategoryCardDTO> suggestions;
    private String resolvedLocation;
    private AppliedFilters appliedFilters;

    public List<AdventureSummaryDTO> getAdventures() {
        return adventures;
    }

    public String getResolvedLocation() {
        return resolvedLocation;
    }

    public void setResolvedLocation(String resolvedLocation) {
        this.resolvedLocation = resolvedLocation;
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
        private Double userLat;
        private Double userLng;
        private String userCity;
        private String sortBy;

        public Long getCategoryId() {
            return categoryId;
        }

        public Double getUserLat() {
            return userLat;
        }

        public void setUserLat(Double userLat) {
            this.userLat = userLat;
        }

        public Double getUserLng() {
            return userLng;
        }

        public void setUserLng(Double userLng) {
            this.userLng = userLng;
        }

        public String getUserCity() {
            return userCity;
        }

        public void setUserCity(String userCity) {
            this.userCity = userCity;
        }

        public String getSortBy() {
            return sortBy;
        }

        public void setSortBy(String sortBy) {
            this.sortBy = sortBy;
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
