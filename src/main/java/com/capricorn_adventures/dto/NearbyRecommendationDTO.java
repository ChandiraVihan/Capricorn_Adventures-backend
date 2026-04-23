package com.capricorn_adventures.dto;

import java.math.BigDecimal;
import java.util.List;

public class NearbyRecommendationDTO {

    // ── "Adventures Near You" (homepage, up to 6) ──────────────────────────────
    private List<RecommendedAdventureDTO> adventuresNearYou;

    // ── "More in This Area" (adventure detail page, up to 4) ───────────────────
    private List<RecommendedAdventureDTO> moreInThisArea;

    /** null when results are within 20 km; set to "Showing results within 50 km" when radius was expanded */
    private String radiusNote;

    /** kilometres used for the moreInThisArea search (20 or 50) */
    private double searchRadiusKm;

    public List<RecommendedAdventureDTO> getAdventuresNearYou() { return adventuresNearYou; }
    public void setAdventuresNearYou(List<RecommendedAdventureDTO> adventuresNearYou) { this.adventuresNearYou = adventuresNearYou; }

    public List<RecommendedAdventureDTO> getMoreInThisArea() { return moreInThisArea; }
    public void setMoreInThisArea(List<RecommendedAdventureDTO> moreInThisArea) { this.moreInThisArea = moreInThisArea; }

    public String getRadiusNote() { return radiusNote; }
    public void setRadiusNote(String radiusNote) { this.radiusNote = radiusNote; }

    public double getSearchRadiusKm() { return searchRadiusKm; }
    public void setSearchRadiusKm(double searchRadiusKm) { this.searchRadiusKm = searchRadiusKm; }

    // ── Nested item DTO ─────────────────────────────────────────────────────────

    public static class RecommendedAdventureDTO {
        private Long id;
        private String name;
        private String description;
        private BigDecimal basePrice;
        private String primaryImageUrl;
        private String location;
        private Long categoryId;
        private String categoryName;
        private Double distanceKm;
        private String estimatedTravelTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

        public String getPrimaryImageUrl() { return primaryImageUrl; }
        public void setPrimaryImageUrl(String primaryImageUrl) { this.primaryImageUrl = primaryImageUrl; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

        public Double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

        public String getEstimatedTravelTime() { return estimatedTravelTime; }
        public void setEstimatedTravelTime(String estimatedTravelTime) { this.estimatedTravelTime = estimatedTravelTime; }
    }
}
