package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.TrailRouteRequestDTO;
import com.capricorn_adventures.dto.TrailRouteResponseDTO;

public interface TrailRouteService {
    TrailRouteResponseDTO getTrailRoute(Long adventureId);
    TrailRouteResponseDTO saveTrailRoute(TrailRouteRequestDTO request);
    TrailRouteResponseDTO updateTrailRoute(Long adventureId, TrailRouteRequestDTO request);
    void deleteTrailRoute(Long adventureId);
}