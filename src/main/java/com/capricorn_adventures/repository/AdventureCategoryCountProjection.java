package com.capricorn_adventures.repository;

public interface AdventureCategoryCountProjection {
    Long getCategoryId();
    String getCategoryName();
    String getThumbnailUrl();
    Long getAdventureCount();
}
