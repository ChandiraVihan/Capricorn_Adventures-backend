package com.capricorn_adventures.dto;

import java.math.BigDecimal;

public class AdventureBrowseRequestDTO {

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
