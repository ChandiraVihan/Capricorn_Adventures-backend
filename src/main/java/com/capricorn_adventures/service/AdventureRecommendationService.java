package com.capricorn_adventures.service;

import com.capricorn_adventures.dto.NearbyRecommendationDTO;
import java.util.UUID;

public interface AdventureRecommendationService {

    /**
     * Returns up to 6 adventures nearest to the user, ranked by distance then rating.
     * Already-completed adventures (CONFIRMED status with past schedule) are excluded
     * when userId is provided.
     *
     * Acceptance criteria:
     *  - AC1: Homepage "Adventures Near You" — up to 6, ranked by distance + rating
     *  - AC2: Completed adventures excluded for authenticated users
     *
     * @param userLat  user latitude  (required)
     * @param userLng  user longitude (required)
     * @param userId   optional — when provided, completed adventures are filtered out
     */
    NearbyRecommendationDTO getAdventuresNearYou(Double userLat, Double userLng, UUID userId);

    /**
     * Returns up to 4 adventures within 20 km of the given adventure's location.
     * If fewer than 1 result is found within 20 km, the radius expands to 50 km
     * and a note is included in the response.
     * The source adventure itself is excluded from results.
     *
     * Acceptance criteria:
     *  - AC3: Adventure detail "More in This Area" — 4 within 20 km
     *  - AC4: Expand to 50 km with note when nothing found within 20 km
     *
     * @param adventureId  the adventure being viewed
     * @param userLat      user latitude (used for distance computation)
     * @param userLng      user longitude
     */
    NearbyRecommendationDTO getMoreInThisArea(Long adventureId, Double userLat, Double userLng);
}
