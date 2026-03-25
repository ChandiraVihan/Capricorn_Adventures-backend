package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.util.List;

public class RoomResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private List<RoomImageDTO> images;
    private List<AmenityDTO> amenities;

    public RoomResponse() {
    }

    public RoomResponse(Long id, String name, String description, BigDecimal basePrice, List<RoomImageDTO> images, List<AmenityDTO> amenities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.images = images;
        this.amenities = amenities;
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

    public List<RoomImageDTO> getImages() {
        return images;
    }

    public void setImages(List<RoomImageDTO> images) {
        this.images = images;
    }

    public List<AmenityDTO> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<AmenityDTO> amenities) {
        this.amenities = amenities;
    }
}
