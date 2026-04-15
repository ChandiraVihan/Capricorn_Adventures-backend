package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.NearbyPoiResponseDTO;

public interface NearbyPoiService {

    /**
     * Returns up to 8 POIs near the adventure's coordinates.
     * Results are cached for 24 hours per adventure.
     * If no POIs are found within 5 km the search radius auto-expands to 10 km.
     *
     * @param adventureId  the adventure whose start coordinates are used
     * @param categoryFilter optional category filter (RESTAURANT, VIEWPOINT, PARKING,
     *                       PETROL_STATION) – null means all categories
     * @return response containing the POI list and the effective search radius
     */
    NearbyPoiResponseDTO getNearbyPois(Long adventureId, String categoryFilter);
}
