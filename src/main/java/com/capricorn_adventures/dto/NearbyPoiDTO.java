package com.capricorn_adventures.dto;

public class NearbyPoiDTO {

    private String placeId;
    private String name;
    private String category;      // RESTAURANT | VIEWPOINT | PARKING | PETROL_STATION
    private String categoryIcon;  // emoji icon for the category
    private double latitude;
    private double longitude;
    private double distanceKm;    // distance from adventure start point
    private String googleMapsUrl; // directions link

    public NearbyPoiDTO() {}

    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryIcon() { return categoryIcon; }
    public void setCategoryIcon(String categoryIcon) { this.categoryIcon = categoryIcon; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl; }
}
