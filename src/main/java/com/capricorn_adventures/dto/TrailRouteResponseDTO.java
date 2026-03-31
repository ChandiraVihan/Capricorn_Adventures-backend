package com.capricorn_adventures.dto;

import java.util.List;

public class TrailRouteResponseDTO {

    private Long adventureId;
    private boolean hasRoute;           // false = frontend skips map rendering entirely
    private List<TrailRoutePointDTO> routePoints;
    private TrailRoutePointDTO startPoint;  // green marker (AC4)
    private TrailRoutePointDTO endPoint;    // red marker (AC4)
    private String staticMapImageUrl;       // fallback image (AC5)

    public Long getAdventureId() { return adventureId; }
    public void setAdventureId(Long adventureId) { this.adventureId = adventureId; }

    public boolean isHasRoute() { return hasRoute; }
    public void setHasRoute(boolean hasRoute) { this.hasRoute = hasRoute; }

    public List<TrailRoutePointDTO> getRoutePoints() { return routePoints; }
    public void setRoutePoints(List<TrailRoutePointDTO> routePoints) { this.routePoints = routePoints; }

    public TrailRoutePointDTO getStartPoint() { return startPoint; }
    public void setStartPoint(TrailRoutePointDTO startPoint) { this.startPoint = startPoint; }

    public TrailRoutePointDTO getEndPoint() { return endPoint; }
    public void setEndPoint(TrailRoutePointDTO endPoint) { this.endPoint = endPoint; }

    public String getStaticMapImageUrl() { return staticMapImageUrl; }
    public void setStaticMapImageUrl(String staticMapImageUrl) { this.staticMapImageUrl = staticMapImageUrl; }
}