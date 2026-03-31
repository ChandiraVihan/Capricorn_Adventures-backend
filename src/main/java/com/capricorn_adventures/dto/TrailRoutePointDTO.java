package com.capricorn_adventures.dto;

public class TrailRoutePointDTO {

    private double lat;
    private double lng;
    private double elevation;         // metres — shown in hover tooltip (AC2)
    private double distanceFromStart; // km — shown in hover tooltip (AC2)

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public double getElevation() { return elevation; }
    public void setElevation(double elevation) { this.elevation = elevation; }

    public double getDistanceFromStart() { return distanceFromStart; }
    public void setDistanceFromStart(double distanceFromStart) { this.distanceFromStart = distanceFromStart; }
}