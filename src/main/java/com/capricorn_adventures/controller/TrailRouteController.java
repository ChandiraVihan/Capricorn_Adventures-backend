package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.TrailRouteRequestDTO;
import com.capricorn_adventures.dto.TrailRouteResponseDTO;
import com.capricorn_adventures.service.TrailRouteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/adventures")
@CrossOrigin(origins = "*")
public class TrailRouteController {

    private final TrailRouteService trailRouteService;

    public TrailRouteController(TrailRouteService trailRouteService) {
        this.trailRouteService = trailRouteService;
    }

    // ── Customer endpoint (public) ────────────────────────────────────────────

    // AC1: map loads when listing page opens
    @GetMapping("/{adventureId}/trail-route")
    public ResponseEntity<TrailRouteResponseDTO> getTrailRoute(
            @PathVariable Long adventureId) {
        return ResponseEntity.ok(trailRouteService.getTrailRoute(adventureId));
    }

    // ── Admin endpoints (protected) ───────────────────────────────────────────

    @PostMapping("/{adventureId}/trail-route")
    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<TrailRouteResponseDTO> createTrailRoute(
            @PathVariable Long adventureId,
            @Valid @RequestBody TrailRouteRequestDTO request) {
        request.setAdventureId(adventureId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trailRouteService.saveTrailRoute(request));
    }

    @PutMapping("/{adventureId}/trail-route")
    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<TrailRouteResponseDTO> updateTrailRoute(
            @PathVariable Long adventureId,
            @Valid @RequestBody TrailRouteRequestDTO request) {
        return ResponseEntity.ok(trailRouteService.updateTrailRoute(adventureId, request));
    }

    @DeleteMapping("/{adventureId}/trail-route")
    @PreAuthorize("hasAuthority('ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrailRoute(
            @PathVariable Long adventureId) {
        trailRouteService.deleteTrailRoute(adventureId);
        return ResponseEntity.noContent().build();
    }
}