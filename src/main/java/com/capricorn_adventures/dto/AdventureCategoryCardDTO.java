package com.capricorn_adventures.dto;

public class AdventureCategoryCardDTO {
    private Long id;
    private String name;
    private String thumbnailUrl;
    private Long adventureCount;

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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getAdventureCount() {
        return adventureCount;
    }

    public void setAdventureCount(Long adventureCount) {
        this.adventureCount = adventureCount;
    }
}
