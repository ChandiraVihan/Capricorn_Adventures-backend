package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.NearbyPoiDTO;
import com.capricorn_adventures.dto.NearbyPoiResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureRepository;
import com.capricorn_adventures.service.NearbyPoiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class NearbyPoiServiceImpl implements NearbyPoiService {

    private static final Logger log = LoggerFactory.getLogger(NearbyPoiServiceImpl.class);

    // ── constants ──────────────────────────────────────────────────────────────
    private static final int    MAX_POIS            = 8;
    private static final double DEFAULT_RADIUS_KM   = 5.0;
    private static final double EXPANDED_RADIUS_KM  = 10.0;
    private static final long   CACHE_TTL_SECONDS   = 24 * 60 * 60; // 24 hours

    /** Supported POI categories and their display metadata. */
    private static final Map<String, String[]> CATEGORY_META = new LinkedHashMap<>();
    static {
        // key → [Places API type, emoji icon, display label]
        CATEGORY_META.put("RESTAURANT",      new String[]{"restaurant",      "🍽️", "Restaurant"});
        CATEGORY_META.put("VIEWPOINT",       new String[]{"tourist_attraction","🏔️", "Viewpoint"});
        CATEGORY_META.put("PARKING",         new String[]{"parking",          "🅿️", "Parking"});
        CATEGORY_META.put("PETROL_STATION",  new String[]{"gas_station",      "⛽", "Petrol Station"});
    }

    // ── in-memory cache ────────────────────────────────────────────────────────
    // key: adventureId  value: [expiry-epoch-seconds, List<NearbyPoiDTO>]
    private static final Map<Long, CacheEntry> poiCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final List<NearbyPoiDTO> pois;
        final double radiusKm;
        final boolean radiusExpanded;
        final long expiresAt;

        CacheEntry(List<NearbyPoiDTO> pois, double radiusKm, boolean radiusExpanded) {
            this.pois           = pois;
            this.radiusKm       = radiusKm;
            this.radiusExpanded = radiusExpanded;
            this.expiresAt      = Instant.now().getEpochSecond() + CACHE_TTL_SECONDS;
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAt;
        }
    }

    // ── dependencies ───────────────────────────────────────────────────────────
    private final AdventureRepository adventureRepository;
    private final HttpClient          httpClient;
    private final ObjectMapper        objectMapper;

    @Value("${google.places.api.key:}")
    private String googleApiKey;

    @Autowired
    public NearbyPoiServiceImpl(AdventureRepository adventureRepository) {
        this.adventureRepository = adventureRepository;
        this.httpClient          = HttpClient.newHttpClient();
        this.objectMapper        = new ObjectMapper();
    }

    // ── public API ─────────────────────────────────────────────────────────────

    @Override
    public NearbyPoiResponseDTO getNearbyPois(Long adventureId, String categoryFilter) {

        Adventure adventure = adventureRepository.findById(adventureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Adventure not found with id: " + adventureId));

        // Adventures must have coordinates stored as "lat,lng" in the location field
        double[] coords = parseCoordinates(adventure);

        // ── serve from cache if still valid ───────────────────────────────────
        CacheEntry cached = poiCache.get(adventureId);
        if (cached != null && !cached.isExpired()) {
            log.debug("POI cache hit for adventure {}", adventureId);
            return buildResponse(cached.pois, cached.radiusKm, cached.radiusExpanded, categoryFilter);
        }

        // ── fetch fresh data ──────────────────────────────────────────────────
        log.debug("POI cache miss for adventure {} – fetching from API", adventureId);
        List<NearbyPoiDTO> pois = fetchPois(coords[0], coords[1], DEFAULT_RADIUS_KM);

        boolean expanded = false;
        double  usedRadius = DEFAULT_RADIUS_KM;

        if (pois.isEmpty()) {
            log.debug("No POIs within {} km for adventure {} – expanding to {} km",
                    DEFAULT_RADIUS_KM, adventureId, EXPANDED_RADIUS_KM);
            pois      = fetchPois(coords[0], coords[1], EXPANDED_RADIUS_KM);
            expanded  = true;
            usedRadius = EXPANDED_RADIUS_KM;
        }

        // cache the full (unfiltered) list
        poiCache.put(adventureId, new CacheEntry(pois, usedRadius, expanded));

        return buildResponse(pois, usedRadius, expanded, categoryFilter);
    }

    // ── private helpers ────────────────────────────────────────────────────────

    /**
     * Fetches POIs for all supported categories, merges them, caps at MAX_POIS,
     * and returns the result sorted by distance.
     */
    private List<NearbyPoiDTO> fetchPois(double lat, double lng, double radiusKm) {
        List<NearbyPoiDTO> all = new ArrayList<>();

        for (Map.Entry<String, String[]> entry : CATEGORY_META.entrySet()) {
            String   category    = entry.getKey();
            String[] meta        = entry.getValue();
            String   placesType  = meta[0];
            String   icon        = meta[1];

            try {
                List<NearbyPoiDTO> results = callPlacesApi(lat, lng, radiusKm, placesType, category, icon);
                all.addAll(results);
            } catch (Exception e) {
                log.warn("Failed to fetch POIs for category {} from Google Places: {}", category, e.getMessage());
            }
        }

        // Sort by distance and cap
        return all.stream()
                .sorted(Comparator.comparingDouble(NearbyPoiDTO::getDistanceKm))
                .limit(MAX_POIS)
                .collect(Collectors.toList());
    }

    /**
     * Calls the Google Places Nearby Search API for a single place type.
     */
    private List<NearbyPoiDTO> callPlacesApi(double lat, double lng, double radiusKm,
                                             String placesType, String category, String icon)
            throws Exception {

        int radiusMeters = (int) (radiusKm * 1000);

        String url = UriComponentsBuilder
                .fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", lat + "," + lng)
                .queryParam("radius",   radiusMeters)
                .queryParam("type",     placesType)
                .queryParam("key",      googleApiKey)
                .toUriString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Google Places API returned HTTP " + response.statusCode());
        }

        return parsePlacesResponse(response.body(), lat, lng, category, icon);
    }

    /**
     * Parses a Google Places Nearby Search JSON response into {@link NearbyPoiDTO} objects.
     */
    private List<NearbyPoiDTO> parsePlacesResponse(String json, double originLat, double originLng,
                                                   String category, String icon) throws Exception {
        JsonNode root    = objectMapper.readTree(json);
        JsonNode results = root.path("results");

        List<NearbyPoiDTO> pois = new ArrayList<>();

        for (JsonNode place : results) {
            NearbyPoiDTO poi = new NearbyPoiDTO();

            String placeId = place.path("place_id").asText();
            double poiLat  = place.path("geometry").path("location").path("lat").asDouble();
            double poiLng  = place.path("geometry").path("location").path("lng").asDouble();

            poi.setPlaceId(placeId);
            poi.setName(place.path("name").asText("Unknown"));
            poi.setCategory(category);
            poi.setCategoryIcon(icon);
            poi.setLatitude(poiLat);
            poi.setLongitude(poiLng);
            poi.setDistanceKm(haversineKm(originLat, originLng, poiLat, poiLng));
            poi.setGoogleMapsUrl(buildMapsUrl(poiLat, poiLng, place.path("name").asText("")));

            pois.add(poi);
        }

        return pois;
    }

    /** Applies optional category filter and wraps into the response DTO. */
    private NearbyPoiResponseDTO buildResponse(List<NearbyPoiDTO> allPois,
                                               double radiusKm,
                                               boolean radiusExpanded,
                                               String categoryFilter) {
        List<NearbyPoiDTO> filtered = allPois;

        if (categoryFilter != null && !categoryFilter.isBlank()) {
            String upper = categoryFilter.toUpperCase(Locale.ROOT);
            if (!CATEGORY_META.containsKey(upper)) {
                throw new IllegalArgumentException(
                        "Unknown POI category: " + categoryFilter +
                                ". Valid values: " + String.join(", ", CATEGORY_META.keySet()));
            }
            filtered = allPois.stream()
                    .filter(p -> upper.equals(p.getCategory()))
                    .collect(Collectors.toList());
        }

        NearbyPoiResponseDTO dto = new NearbyPoiResponseDTO();
        dto.setPois(filtered);
        dto.setSearchRadiusKm(radiusKm);
        dto.setRadiusExpanded(radiusExpanded);
        return dto;
    }

    /**
     * Parses the adventure's location field.
     * Expects the format {@code "lat,lng"} (e.g. {@code "6.9271,79.8612"}).
     * Falls back to geocoding the location name string if coordinates are absent.
     *
     * @throws ResourceNotFoundException when no usable coordinates can be found
     */
    private double[] parseCoordinates(Adventure adventure) {
        String location = adventure.getLocation();

        if (location != null && !location.isBlank()) {
            String[] parts = location.split(",");
            if (parts.length == 2) {
                try {
                    double lat = Double.parseDouble(parts[0].trim());
                    double lng = Double.parseDouble(parts[1].trim());
                    if (lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180) {
                        return new double[]{lat, lng};
                    }
                } catch (NumberFormatException ignored) {
                    // location is a place name, not coordinates – fall through
                }
            }
        }

        throw new ResourceNotFoundException(
                "Adventure " + adventure.getId() +
                        " does not have valid coordinates. " +
                        "Store the start point as \"lat,lng\" in the location field to enable nearby POIs.");
    }

    /** Google Maps directions deep-link. */
    private String buildMapsUrl(double lat, double lng, String name) {
        return "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng
                + "&destination_place_name=" + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Haversine formula – returns distance in km. */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
