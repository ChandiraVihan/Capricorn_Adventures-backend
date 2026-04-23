package com.capricorn_adventures.controller;

import com.capricorn_adventures.dto.NearbyRecommendationDTO;
import com.capricorn_adventures.security.JwtUtil;
import com.capricorn_adventures.service.AdventureRecommendationService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for location-based adventure recommendations.
 *
 * Endpoints
 * ---------
 * GET /api/recommendations/near-you
 *     AC1: Homepage "Adventures Near You" — up to 6 adventures ranked by distance + rating.
 *     AC2: When the caller is authenticated, completed adventures are excluded.
 *
 * GET /api/recommendations/adventures/{adventureId}/more-in-area
 *     AC3: Adventure detail "More in This Area" — up to 4 adventures within 20 km.
 *     AC4: If nothing found within 20 km, expands to 50 km with an explanatory note.
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class AdventureRecommendationController {

    private final AdventureRecommendationService recommendationService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AdventureRecommendationController(AdventureRecommendationService recommendationService,
                                             JwtUtil jwtUtil) {
        this.recommendationService = recommendationService;
        this.jwtUtil = jwtUtil;
    }

    // ── AC1 + AC2 ──────────────────────────────────────────────────────────────

    /**
     * Homepage "Adventures Near You".
     *
     * Query params:
     *   userLat  — user's latitude  (required)
     *   userLng  — user's longitude (required)
     *
     * Optional header:
     *   Authorization: Bearer <jwt>  — when present, completed adventures are excluded (AC2).
     *
     * Response fields used by this endpoint:
     *   adventuresNearYou  — up to 6 items, each with distanceKm & estimatedTravelTime
     *
     * Example:
     *   GET /api/recommendations/near-you?userLat=6.9271&userLng=79.8612
     */
    @GetMapping("/near-you")
    public ResponseEntity<NearbyRecommendationDTO> getAdventuresNearYou(
            @RequestParam Double userLat,
            @RequestParam Double userLng,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        UUID userId = extractUserIdFromToken(authHeader);
        NearbyRecommendationDTO response = recommendationService.getAdventuresNearYou(userLat, userLng, userId);
        return ResponseEntity.ok(response);
    }

    // ── AC3 + AC4 ──────────────────────────────────────────────────────────────

    /**
     * Adventure detail "More in This Area".
     *
     * Path variable:
     *   adventureId — the adventure currently being viewed
     *
     * Query params:
     *   userLat  — user's latitude  (required; used by DistanceMockService)
     *   userLng  — user's longitude (required)
     *
     * Response fields used by this endpoint:
     *   moreInThisArea  — up to 4 items sorted by distance ASC
     *   searchRadiusKm  — 20.0 normally, 50.0 when expanded
     *   radiusNote      — null normally; set to an explanatory message when radius expanded (AC4)
     *
     * Example:
     *   GET /api/recommendations/adventures/42/more-in-area?userLat=6.9271&userLng=79.8612
     */
    @GetMapping("/adventures/{adventureId}/more-in-area")
    public ResponseEntity<NearbyRecommendationDTO> getMoreInThisArea(
            @PathVariable Long adventureId,
            @RequestParam Double userLat,
            @RequestParam Double userLng) {

        NearbyRecommendationDTO response = recommendationService.getMoreInThisArea(adventureId, userLat, userLng);
        return ResponseEntity.ok(response);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Extracts the user UUID from a Bearer JWT header.
     * Returns null if the header is absent, malformed, or the token is invalid —
     * this is safe because the recommendation endpoints are public; the userId is only
     * used to filter out completed adventures (AC2) and gracefully degrades when absent.
     */
    private UUID extractUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        } catch (Exception e) {
            // Invalid / expired token → treat as unauthenticated
            return null;
        }
    }
}
