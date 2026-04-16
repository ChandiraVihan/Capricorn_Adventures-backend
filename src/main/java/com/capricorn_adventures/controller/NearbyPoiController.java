package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.NearbyPoiResponseDTO;
import com.capricorn_adventures.service.NearbyPoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Exposes nearby points of interest for an adventure listing.
 *
 * GET /api/adventures/{adventureId}/nearby-pois
 *      ?category=RESTAURANT|VIEWPOINT|PARKING|PETROL_STATION  (optional)
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NearbyPoiController {

    private final NearbyPoiService nearbyPoiService;

    @Autowired
    public NearbyPoiController(NearbyPoiService nearbyPoiService) {
        this.nearbyPoiService = nearbyPoiService;
    }

    @GetMapping("/adventures/{adventureId}/nearby-pois")
    public ResponseEntity<NearbyPoiResponseDTO> getNearbyPois(
            @PathVariable Long adventureId,
            @RequestParam(required = false) String category) {

        NearbyPoiResponseDTO response = nearbyPoiService.getNearbyPois(adventureId, category);
        return ResponseEntity.ok(response);
    }
}
