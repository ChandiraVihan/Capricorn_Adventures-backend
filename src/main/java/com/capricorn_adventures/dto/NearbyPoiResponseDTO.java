package com.capricorn_adventures.dto;

import java.util.List;

public class NearbyPoiResponseDTO {

    private List<NearbyPoiDTO> pois;
    private double searchRadiusKm;   // 5 or 10 (auto-expanded)
    private boolean radiusExpanded;  // true when no results within 5 km

    public NearbyPoiResponseDTO() {}

    public List<NearbyPoiDTO> getPois() { return pois; }
    public void setPois(List<NearbyPoiDTO> pois) { this.pois = pois; }

    public double getSearchRadiusKm() { return searchRadiusKm; }
    public void setSearchRadiusKm(double searchRadiusKm) { this.searchRadiusKm = searchRadiusKm; }

    public boolean isRadiusExpanded() { return radiusExpanded; }
    public void setRadiusExpanded(boolean radiusExpanded) { this.radiusExpanded = radiusExpanded; }
}
