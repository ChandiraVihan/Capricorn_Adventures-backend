package com.capricorn_adventures.dto;

public class AmenityDTO {

    private Long id;
    private String name;
    private String iconIdentifier;

    public AmenityDTO() {
    }

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

    public String getIconIdentifier() {
        return iconIdentifier;
    }

    public void setIconIdentifier(String iconIdentifier) {
        this.iconIdentifier = iconIdentifier;
    }
}
