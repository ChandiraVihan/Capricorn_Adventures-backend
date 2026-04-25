package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotBlank;

public class AssignGuideRequestDTO {

    @NotBlank(message = "guideName is required")
    private String guideName;

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }
}
