package com.capricorn_adventures.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.capricorn_adventures.dto.TrailRoutePointDTO;
import com.capricorn_adventures.dto.TrailRouteRequestDTO;
import com.capricorn_adventures.dto.TrailRouteResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.TrailRoute;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.repository.TrailRouteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TrailRouteServiceImplTest {

    @Mock
    private TrailRouteRepository trailRouteRepository;

    @Mock
    private AdventureRepository adventureRepository;

    private TrailRouteServiceImpl service;

    // A real ObjectMapper so JSON serialisation actually runs
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = new TrailRouteServiceImpl(trailRouteRepository, adventureRepository, objectMapper);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private Adventure stubAdventure(Long id) {
        Adventure a = new Adventure();
        a.setId(id);
        return a;
    }

    private TrailRoutePointDTO point(double lat, double lng, double elev, double dist) {
        TrailRoutePointDTO p = new TrailRoutePointDTO();
        p.setLat(lat);
        p.setLng(lng);
        p.setElevation(elev);
        p.setDistanceFromStart(dist);
        return p;
    }

    private TrailRoute savedRoute(Long adventureId, List<TrailRoutePointDTO> points, String imageUrl)
            throws Exception {
        TrailRoute r = new TrailRoute();
        r.setAdventureId(adventureId);
        r.setRoutePointsJson(objectMapper.writeValueAsString(points));
        r.setStaticMapImageUrl(imageUrl);
        return r;
    }

    private TrailRouteRequestDTO request(Long adventureId,
                                         List<TrailRoutePointDTO> points,
                                         String imageUrl) {
        TrailRouteRequestDTO req = new TrailRouteRequestDTO();
        req.setAdventureId(adventureId);
        req.setRoutePoints(points);
        req.setStaticMapImageUrl(imageUrl);
        return req;
    }

    // ── AC1: getTrailRoute ─────────────────────────────────────────────────────

    @Test
    void getTrailRoute_whenRouteExists_returnsHasRouteTrue() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(
                point(6.9271, 79.8612, 10.0, 0.0),
                point(6.9300, 79.8650, 25.0, 0.5)
        );
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, "https://cdn.example.com/static.png")));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertTrue(result.isHasRoute());
        assertEquals(1L, result.getAdventureId());
        assertEquals(2, result.getRoutePoints().size());
    }

    @Test
    void getTrailRoute_whenNoRouteConfigured_returnsHasRouteFalse() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L)).thenReturn(Optional.empty());

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertFalse(result.isHasRoute());
        assertTrue(result.getRoutePoints().isEmpty());
    }

    @Test
    void getTrailRoute_whenAdventureNotFound_throwsResourceNotFoundException() {
        when(adventureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getTrailRoute(99L));
    }

    // ── AC2: elevation + distanceFromStart present in every point ─────────────

    @Test
    void getTrailRoute_routePointsCarryElevationAndDistance() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(
                point(6.9271, 79.8612, 12.5, 0.0),
                point(6.9290, 79.8630, 18.3, 0.3),
                point(6.9310, 79.8660, 22.0, 0.7)
        );
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, null)));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertEquals(12.5, result.getRoutePoints().get(0).getElevation());
        assertEquals(0.0,  result.getRoutePoints().get(0).getDistanceFromStart());
        assertEquals(18.3, result.getRoutePoints().get(1).getElevation());
        assertEquals(0.3,  result.getRoutePoints().get(1).getDistanceFromStart());
    }

    // ── AC4: start and end markers ─────────────────────────────────────────────

    @Test
    void getTrailRoute_firstPointIsStartLastPointIsEnd() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(
                point(6.9271, 79.8612, 10.0, 0.0),   // start
                point(6.9285, 79.8635, 15.0, 0.3),
                point(6.9310, 79.8660, 20.0, 0.8)    // end
        );
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, null)));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertNotNull(result.getStartPoint());
        assertNotNull(result.getEndPoint());
        assertEquals(6.9271, result.getStartPoint().getLat());
        assertEquals(6.9310, result.getEndPoint().getLat());
    }

    @Test
    void getTrailRoute_singlePointRoute_startAndEndAreSamePoint() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(point(6.9271, 79.8612, 10.0, 0.0));
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, null)));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertEquals(result.getStartPoint().getLat(), result.getEndPoint().getLat());
    }

    // ── AC5: static fallback image ─────────────────────────────────────────────

    @Test
    void getTrailRoute_staticMapImageUrlIsReturnedWhenSet() throws Exception {
        String url = "https://cdn.example.com/fallback.png";
        List<TrailRoutePointDTO> pts = List.of(point(6.9271, 79.8612, 10.0, 0.0));
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, url)));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertEquals(url, result.getStaticMapImageUrl());
    }

    @Test
    void getTrailRoute_staticMapImageUrlIsNullWhenNotSet() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(point(6.9271, 79.8612, 10.0, 0.0));
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.findByAdventureId(1L))
                .thenReturn(Optional.of(savedRoute(1L, pts, null)));

        TrailRouteResponseDTO result = service.getTrailRoute(1L);

        assertNull(result.getStaticMapImageUrl());
    }

    // ── saveTrailRoute ─────────────────────────────────────────────────────────

    @Test
    void saveTrailRoute_persistsAndReturnsDto() throws Exception {
        List<TrailRoutePointDTO> pts = List.of(
                point(6.9271, 79.8612, 10.0, 0.0),
                point(6.9310, 79.8660, 20.0, 0.8)
        );
        TrailRouteRequestDTO req = request(1L, pts, "https://cdn.example.com/static.png");

        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.existsByAdventureId(1L)).thenReturn(false);
        when(trailRouteRepository.save(any(TrailRoute.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TrailRouteResponseDTO result = service.saveTrailRoute(req);

        assertTrue(result.isHasRoute());
        assertEquals(2, result.getRoutePoints().size());
        verify(trailRouteRepository).save(any(TrailRoute.class));
    }

    @Test
    void saveTrailRoute_whenRouteAlreadyExists_throwsIllegalStateException() {
        List<TrailRoutePointDTO> pts = List.of(point(6.9271, 79.8612, 10.0, 0.0));
        TrailRouteRequestDTO req = request(1L, pts, null);

        when(adventureRepository.findById(1L)).thenReturn(Optional.of(stubAdventure(1L)));
        when(trailRouteRepository.existsByAdventureId(1L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.saveTrailRoute(req));
        verify(trailRouteRepository, never()).save(any());
    }

    // ── updateTrailRoute ───────────────────────────────────────────────────────

    @Test
    void updateTrailRoute_updatesPointsAndFallbackUrl() throws Exception {
        List<TrailRoutePointDTO> original = List.of(point(6.9271, 79.8612, 10.0, 0.0));
        TrailRoute existing = savedRoute(1L, original, null);

        List<TrailRoutePointDTO> updated = List.of(
                point(6.9271, 79.8612, 10.0, 0.0),
                point(6.9310, 79.8660, 20.0, 0.8)
        );
        TrailRouteRequestDTO req = request(1L, updated, "https://cdn.example.com/new.png");

        when(trailRouteRepository.findByAdventureId(1L)).thenReturn(Optional.of(existing));
        when(trailRouteRepository.save(any(TrailRoute.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TrailRouteResponseDTO result = service.updateTrailRoute(1L, req);

        assertEquals(2, result.getRoutePoints().size());
        assertEquals("https://cdn.example.com/new.png", result.getStaticMapImageUrl());
    }

    @Test
    void updateTrailRoute_whenRouteNotFound_throwsResourceNotFoundException() {
        when(trailRouteRepository.findByAdventureId(99L)).thenReturn(Optional.empty());
        TrailRouteRequestDTO req = request(99L, List.of(), null);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateTrailRoute(99L, req));
    }

    // ── deleteTrailRoute ───────────────────────────────────────────────────────

    @Test
    void deleteTrailRoute_callsRepositoryDelete() {
        when(trailRouteRepository.existsByAdventureId(1L)).thenReturn(true);

        service.deleteTrailRoute(1L);

        verify(trailRouteRepository).deleteByAdventureId(1L);
    }

    @Test
    void deleteTrailRoute_whenRouteNotFound_throwsResourceNotFoundException() {
        when(trailRouteRepository.existsByAdventureId(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.deleteTrailRoute(99L));
        verify(trailRouteRepository, never()).deleteByAdventureId(any());
    }
}
