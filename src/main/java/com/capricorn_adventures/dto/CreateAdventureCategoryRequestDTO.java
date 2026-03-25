package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateAdventureCategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotBlank(message = "Category thumbnail URL is required")
    private String thumbnailUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}