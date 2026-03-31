package com.capricorn_adventures.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class TrailRouteRequestDTO {

    private Long adventureId; // set from path variable in controller

    @NotNull(message = "Route points are required")
    @NotEmpty(message = "Route must have at least one point")
    private List<TrailRoutePointDTO> routePoints;

    // Optional static image fallback (AC5)
    private String staticMapImageUrl;

    public Long getAdventureId() { return adventureId; }
    public void setAdventureId(Long adventureId) { this.adventureId = adventureId; }

    public List<TrailRoutePointDTO> getRoutePoints() { return routePoints; }
    public void setRoutePoints(List<TrailRoutePointDTO> routePoints) { this.routePoints = routePoints; }

    public String getStaticMapImageUrl() { return staticMapImageUrl; }
    public void setStaticMapImageUrl(String staticMapImageUrl) { this.staticMapImageUrl = staticMapImageUrl; }
}