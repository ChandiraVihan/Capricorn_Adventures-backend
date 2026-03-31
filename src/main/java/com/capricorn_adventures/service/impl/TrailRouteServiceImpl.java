package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.TrailRoutePointDTO;
import com.capricorn_adventures.dto.TrailRouteRequestDTO;
import com.capricorn_adventures.dto.TrailRouteResponseDTO;
import com.capricorn_adventures.entity.TrailRoute;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.repository.TrailRouteRepository;
import com.capricorn_adventures.service.TrailRouteService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class TrailRouteServiceImpl implements TrailRouteService {

    private final TrailRouteRepository trailRouteRepository;
    private final AdventureRepository adventureRepository;
    private final ObjectMapper objectMapper;

    public TrailRouteServiceImpl(TrailRouteRepository trailRouteRepository,
                                 AdventureRepository adventureRepository,
                                 ObjectMapper objectMapper) {
        this.trailRouteRepository = trailRouteRepository;
        this.adventureRepository = adventureRepository;
        this.objectMapper = objectMapper;
    }

    // Customer-facing: GET trail route for a listing page (AC1)
    @Override
    public TrailRouteResponseDTO getTrailRoute(Long adventureId) {
        // Verify adventure exists
        adventureRepository.findById(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found with id: " + adventureId));

        return trailRouteRepository.findByAdventureId(adventureId)
                .map(this::toResponseDTO)
                .orElseGet(() -> emptyResponse(adventureId)); // hasRoute=false if no route configured
    }

    // Admin: create trail route
    @Override
    @Transactional
    public TrailRouteResponseDTO saveTrailRoute(TrailRouteRequestDTO request) {
        adventureRepository.findById(request.getAdventureId())
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found with id: " + request.getAdventureId()));

        if (trailRouteRepository.existsByAdventureId(request.getAdventureId())) {
            throw new IllegalStateException("Trail route already exists for adventure " + request.getAdventureId() + ". Use PUT to update.");
        }

        TrailRoute route = new TrailRoute();
        route.setAdventureId(request.getAdventureId());
        route.setRoutePointsJson(toJson(request.getRoutePoints()));
        route.setStaticMapImageUrl(request.getStaticMapImageUrl());

        return toResponseDTO(trailRouteRepository.save(route));
    }

    // Admin: update trail route
    @Override
    @Transactional
    public TrailRouteResponseDTO updateTrailRoute(Long adventureId, TrailRouteRequestDTO request) {
        TrailRoute route = trailRouteRepository.findByAdventureId(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException("No trail route found for adventure: " + adventureId));

        if (request.getRoutePoints() != null) {
            route.setRoutePointsJson(toJson(request.getRoutePoints()));
        }
        if (request.getStaticMapImageUrl() != null) {
            route.setStaticMapImageUrl(request.getStaticMapImageUrl());
        }

        return toResponseDTO(trailRouteRepository.save(route));
    }

    // Admin: delete trail route
    @Override
    @Transactional
    public void deleteTrailRoute(Long adventureId) {
        if (!trailRouteRepository.existsByAdventureId(adventureId)) {
            throw new ResourceNotFoundException("No trail route found for adventure: " + adventureId);
        }
        trailRouteRepository.deleteByAdventureId(adventureId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private TrailRouteResponseDTO toResponseDTO(TrailRoute route) {
        List<TrailRoutePointDTO> points = fromJson(route.getRoutePointsJson());

        TrailRouteResponseDTO dto = new TrailRouteResponseDTO();
        dto.setAdventureId(route.getAdventureId());
        dto.setHasRoute(true);
        dto.setRoutePoints(points);
        dto.setStaticMapImageUrl(route.getStaticMapImageUrl());

        // AC4: first point = green start, last point = red end
        if (!points.isEmpty()) {
            dto.setStartPoint(points.get(0));
            dto.setEndPoint(points.get(points.size() - 1));
        }

        return dto;
    }

    private TrailRouteResponseDTO emptyResponse(Long adventureId) {
        TrailRouteResponseDTO dto = new TrailRouteResponseDTO();
        dto.setAdventureId(adventureId);
        dto.setHasRoute(false);
        dto.setRoutePoints(Collections.emptyList());
        return dto;
    }

    private String toJson(List<TrailRoutePointDTO> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize route points", e);
        }
    }

    private List<TrailRoutePointDTO> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<TrailRoutePointDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize route points", e);
        }
    }
}