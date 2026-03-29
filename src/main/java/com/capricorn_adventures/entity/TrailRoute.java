package com.capricorn_adventures.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trail_routes")
public class TrailRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links to Adventure without modifying Adventure.java
    @Column(name = "adventure_id", nullable = false, unique = true)
    private Long adventureId;

    // JSON array: [{"lat":..., "lng":..., "elevation":..., "distanceFromStart":...}, ...]
    @Column(columnDefinition = "TEXT", nullable = false)
    private String routePointsJson;

    // Fallback static image URL if interactive map fails to load (AC5)
    @Column(nullable = true)
    private String staticMapImageUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAdventureId() { return adventureId; }
    public void setAdventureId(Long adventureId) { this.adventureId = adventureId; }

    public String getRoutePointsJson() { return routePointsJson; }
    public void setRoutePointsJson(String routePointsJson) { this.routePointsJson = routePointsJson; }

    public String getStaticMapImageUrl() { return staticMapImageUrl; }
    public void setStaticMapImageUrl(String staticMapImageUrl) { this.staticMapImageUrl = staticMapImageUrl; }
}