package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.NearbyRecommendationDTO;
import com.capricorn_adventures.dto.NearbyRecommendationDTO.RecommendedAdventureDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.entity.AdventureCheckoutBooking;
import com.capricorn_adventures.entity.AdventureCheckoutStatus;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureCheckoutBookingRepository;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.service.AdventureRecommendationService;
import com.capricorn_adventures.service.DistanceMockService;
import com.capricorn_adventures.service.DistanceMockService.DistanceResult;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdventureRecommendationServiceImpl implements AdventureRecommendationService {

    /** Maximum adventures returned on the homepage "Near You" section (AC1) */
    private static final int NEAR_YOU_LIMIT = 6;

    /** Maximum adventures returned in the "More in This Area" section (AC3) */
    private static final int MORE_IN_AREA_LIMIT = 4;

    /** Primary search radius in km for "More in This Area" (AC3) */
    private static final double PRIMARY_RADIUS_KM = 20.0;

    /** Fallback search radius when nothing is found within the primary radius (AC4) */
    private static final double FALLBACK_RADIUS_KM = 50.0;

    /** Note shown to the user when the radius is expanded (AC4) */
    private static final String EXPANDED_RADIUS_NOTE =
            "No adventures found within 20 km. Showing results within 50 km.";

    private final AdventureRepository adventureRepository;
    private final AdventureCheckoutBookingRepository bookingRepository;
    private final DistanceMockService distanceMockService;

    @Autowired
    public AdventureRecommendationServiceImpl(
            AdventureRepository adventureRepository,
            AdventureCheckoutBookingRepository bookingRepository,
            DistanceMockService distanceMockService) {
        this.adventureRepository = adventureRepository;
        this.bookingRepository = bookingRepository;
        this.distanceMockService = distanceMockService;
    }

    // ── AC1 + AC2 ──────────────────────────────────────────────────────────────

    /**
     * Homepage "Adventures Near You":
     *  - All active adventures with available schedules are candidates.
     *  - If userId is provided, adventures the user has already completed are excluded (AC2).
     *  - Each candidate is enriched with distance via DistanceMockService.
     *  - Results are ranked: primary sort = distance ASC, secondary = "rating" proxy
     *    (we rank by name descending as a deterministic stand-in until a real rating
     *    column is added to the Adventure entity).
     *  - Capped at 6 (AC1).
     */
    @Override
    public NearbyRecommendationDTO getAdventuresNearYou(Double userLat, Double userLng, UUID userId) {

        // Fetch every active, available adventure
        List<Adventure> allAdventures = adventureRepository.findBrowseAdventures(null, null, null);

        // AC2 — build the set of adventure IDs the user has already completed
        Set<Long> completedAdventureIds = resolveCompletedAdventureIds(userId);

        // Enrich with distance, filter out adventures without a computable distance,
        // exclude completed adventures, then sort and cap.
        List<RecommendedAdventureDTO> nearYou = allAdventures.stream()
                // AC2: exclude already-completed
                .filter(a -> !completedAdventureIds.contains(a.getId()))
                // enrich with distance
                .map(a -> enrichWithDistance(a, userLat, userLng, null))
                // keep only those for which a distance could be calculated
                .filter(dto -> dto.getDistanceKm() != null)
                // AC1: rank by distance ASC, then name DESC as a rating proxy
                .sorted(Comparator.comparingDouble(RecommendedAdventureDTO::getDistanceKm)
                        .thenComparing(Comparator.comparing(RecommendedAdventureDTO::getName).reversed()))
                // AC1: up to 6
                .limit(NEAR_YOU_LIMIT)
                .collect(Collectors.toList());

        NearbyRecommendationDTO response = new NearbyRecommendationDTO();
        response.setAdventuresNearYou(nearYou);
        response.setSearchRadiusKm(0); // not applicable for homepage endpoint
        return response;
    }

    // ── AC3 + AC4 ──────────────────────────────────────────────────────────────

    /**
     * Adventure detail "More in This Area":
     *  - The source adventure's location is used as the geographic reference.
     *  - All other active adventures are enriched with distance from that reference point.
     *  - First pass: keep those within 20 km (AC3).
     *  - If that yields no results, expand to 50 km and attach a note (AC4).
     *  - Results are sorted by distance ASC and capped at 4.
     *  - The source adventure itself is always excluded.
     */
    @Override
    public NearbyRecommendationDTO getMoreInThisArea(Long adventureId, Double userLat, Double userLng) {

        // Resolve the source adventure so we have its location string
        Adventure sourceAdventure = adventureRepository.findByIdWithDetails(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException("Adventure not found with ID: " + adventureId));

        String sourceLocation = sourceAdventure.getLocation();

        // All active/available adventures except the one being viewed
        List<Adventure> candidates = adventureRepository.findBrowseAdventures(null, null, null)
                .stream()
                .filter(a -> !a.getId().equals(adventureId))
                .collect(Collectors.toList());

        // Enrich: compute distance from the source adventure's location to each candidate.
        // We pass the source location as userCity so DistanceMockService uses it as origin.
        List<RecommendedAdventureDTO> enriched = candidates.stream()
                .map(a -> enrichWithDistance(a, userLat, userLng, sourceLocation))
                .filter(dto -> dto.getDistanceKm() != null)
                .collect(Collectors.toList());

        // AC3: try 20 km first
        List<RecommendedAdventureDTO> within20 = filterByRadius(enriched, PRIMARY_RADIUS_KM);

        NearbyRecommendationDTO response = new NearbyRecommendationDTO();

        if (!within20.isEmpty()) {
            // AC3: results found within 20 km
            response.setMoreInThisArea(topN(within20, MORE_IN_AREA_LIMIT));
            response.setSearchRadiusKm(PRIMARY_RADIUS_KM);
            response.setRadiusNote(null);
        } else {
            // AC4: nothing within 20 km → expand to 50 km with explanatory note
            List<RecommendedAdventureDTO> within50 = filterByRadius(enriched, FALLBACK_RADIUS_KM);
            response.setMoreInThisArea(topN(within50, MORE_IN_AREA_LIMIT));
            response.setSearchRadiusKm(FALLBACK_RADIUS_KM);
            response.setRadiusNote(EXPANDED_RADIUS_NOTE);
        }

        return response;
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Returns the set of adventure IDs the authenticated user has already completed.
     * A booking is "completed" when it is CONFIRMED and its schedule's end date is in the past.
     * Returns an empty set when userId is null (unauthenticated request).
     */
    private Set<Long> resolveCompletedAdventureIds(UUID userId) {
        if (userId == null) {
            return Set.of();
        }

        LocalDateTime now = LocalDateTime.now();

        return bookingRepository.findByUserIdWithDetailsOrderByCreatedAtDesc(userId)
                .stream()
                // Only CONFIRMED bookings whose schedule has already ended count as "completed"
                .filter(b -> AdventureCheckoutStatus.CONFIRMED.equals(b.getStatus()))
                .filter(b -> b.getSchedule() != null
                        && b.getSchedule().getEndDate() != null
                        && b.getSchedule().getEndDate().isBefore(now))
                .map(AdventureCheckoutBooking::getAdventure)
                .map(Adventure::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Maps an Adventure to a RecommendedAdventureDTO and attaches distance information.
     *
     * @param adventure      the adventure to enrich
     * @param userLat        user's latitude (may be null)
     * @param userLng        user's longitude (may be null)
     * @param originOverride when non-null, used as the "city" origin instead of user coords
     *                       (used for "More in This Area" where origin = source adventure location)
     */
    private RecommendedAdventureDTO enrichWithDistance(Adventure adventure,
                                                       Double userLat,
                                                       Double userLng,
                                                       String originOverride) {

        RecommendedAdventureDTO dto = mapToDTO(adventure);

        DistanceResult result;
        if (originOverride != null && !originOverride.isBlank()) {
            // Use the source adventure's location string as the reference city
            result = distanceMockService.calculateDistance(
                    adventure.getLocation(), null, null, originOverride);
        } else {
            result = distanceMockService.calculateDistance(
                    adventure.getLocation(), userLat, userLng, null);
        }

        if (result != null) {
            dto.setDistanceKm(result.getDistanceKm());
            dto.setEstimatedTravelTime(result.getEstimatedTravelTime());
        }

        return dto;
    }

    /** Keeps only DTOs whose distance is within the given radius (inclusive). */
    private List<RecommendedAdventureDTO> filterByRadius(List<RecommendedAdventureDTO> dtos, double radiusKm) {
        return dtos.stream()
                .filter(dto -> dto.getDistanceKm() != null && dto.getDistanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(RecommendedAdventureDTO::getDistanceKm))
                .collect(Collectors.toList());
    }

    /** Returns the first n items from an already-sorted list, capped at n. */
    private List<RecommendedAdventureDTO> topN(List<RecommendedAdventureDTO> sorted, int n) {
        return sorted.stream().limit(n).collect(Collectors.toList());
    }

    /** Converts an Adventure entity to a RecommendedAdventureDTO (without distance). */
    private RecommendedAdventureDTO mapToDTO(Adventure adventure) {
        RecommendedAdventureDTO dto = new RecommendedAdventureDTO();
        dto.setId(adventure.getId());
        dto.setName(adventure.getName());
        dto.setDescription(adventure.getDescription());
        dto.setBasePrice(adventure.getBasePrice());
        dto.setPrimaryImageUrl(adventure.getPrimaryImageUrl());
        dto.setLocation(adventure.getLocation());
        if (adventure.getCategory() != null) {
            dto.setCategoryId(adventure.getCategory().getId());
            dto.setCategoryName(adventure.getCategory().getName());
        }
        return dto;
    }
}
