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
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Fetches nearby POIs using the OpenStreetMap Overpass API.
 * Completely free — no API key or billing account required.
 */
@Service
public class NearbyPoiServiceImpl implements NearbyPoiService {

    private static final Logger log = LoggerFactory.getLogger(NearbyPoiServiceImpl.class);

    // ── constants ──────────────────────────────────────────────────────────────
    private static final int    MAX_POIS           = 8;
    private static final double DEFAULT_RADIUS_KM  = 5.0;
    private static final double EXPANDED_RADIUS_KM = 10.0;
    private static final long   CACHE_TTL_SECONDS  = 24 * 60 * 60; // 24 hours

    private static final String OVERPASS_URL = "https://overpass-api.de/api/interpreter";

    /**
     * Category metadata:
     *   key  → [OSM amenity/tourism tag value, emoji icon]
     * The OSM tag type (amenity vs tourism) is handled in buildOverpassQuery().
     */
    private static final Map<String, String[]> CATEGORY_META = new LinkedHashMap<>();
    static {
        CATEGORY_META.put("RESTAURANT",     new String[]{"restaurant",    "🍽️"});
        CATEGORY_META.put("VIEWPOINT",      new String[]{"viewpoint",     "🏔️"});
        CATEGORY_META.put("PARKING",        new String[]{"parking",       "🅿️"});
        CATEGORY_META.put("PETROL_STATION", new String[]{"fuel",          "⛽"});
    }

    // ── in-memory 24-hour cache ────────────────────────────────────────────────
    private static final Map<Long, CacheEntry> poiCache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final List<NearbyPoiDTO> pois;
        final double             radiusKm;
        final boolean            radiusExpanded;
        final long               expiresAt;

        CacheEntry(List<NearbyPoiDTO> pois, double radiusKm, boolean radiusExpanded) {
            this.pois          = pois;
            this.radiusKm      = radiusKm;
            this.radiusExpanded = radiusExpanded;
            this.expiresAt     = Instant.now().getEpochSecond() + CACHE_TTL_SECONDS;
        }

        boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAt;
        }
    }

    // ── dependencies ───────────────────────────────────────────────────────────
    private final AdventureRepository adventureRepository;
    private final HttpClient          httpClient;
    private final ObjectMapper        objectMapper;

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

        double[] coords = parseCoordinates(adventure);

        // serve from cache if still valid
        CacheEntry cached = poiCache.get(adventureId);
        if (cached != null && !cached.isExpired()) {
            log.debug("POI cache hit for adventure {}", adventureId);
            return buildResponse(cached.pois, cached.radiusKm, cached.radiusExpanded, categoryFilter);
        }

        // fetch fresh data at default radius
        log.debug("POI cache miss for adventure {} – fetching from Overpass API", adventureId);
        List<NearbyPoiDTO> pois = fetchPois(coords[0], coords[1], DEFAULT_RADIUS_KM);

        boolean expanded  = false;
        double  usedRadius = DEFAULT_RADIUS_KM;

        // auto-expand if nothing found within 5 km
        if (pois.isEmpty()) {
            log.debug("No POIs within {} km for adventure {} – expanding to {} km",
                    DEFAULT_RADIUS_KM, adventureId, EXPANDED_RADIUS_KM);
            pois       = fetchPois(coords[0], coords[1], EXPANDED_RADIUS_KM);
            expanded   = true;
            usedRadius = EXPANDED_RADIUS_KM;
        }

        poiCache.put(adventureId, new CacheEntry(pois, usedRadius, expanded));
        return buildResponse(pois, usedRadius, expanded, categoryFilter);
    }

    // ── private helpers ────────────────────────────────────────────────────────

    /**
     * Calls Overpass API once with a single query that fetches all four
     * category types in one round-trip, then parses and sorts the results.
     */
    private List<NearbyPoiDTO> fetchPois(double lat, double lng, double radiusKm) {
        int radiusMeters = (int) (radiusKm * 1000);
        String query = buildOverpassQuery(lat, lng, radiusMeters);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OVERPASS_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("data=" +
                            java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8)))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Overpass API returned HTTP {}", response.statusCode());
                return Collections.emptyList();
            }

            return parseOverpassResponse(response.body(), lat, lng);

        } catch (Exception e) {
            log.warn("Overpass API call failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Builds an Overpass QL query that retrieves all four POI types in one request.
     *
     * OSM tags used:
     *   amenity=restaurant   → RESTAURANT
     *   tourism=viewpoint    → VIEWPOINT
     *   amenity=parking      → PARKING
     *   amenity=fuel         → PETROL_STATION
     */
    private String buildOverpassQuery(double lat, double lng, int radiusMeters) {
        return "[out:json][timeout:10];\n" +
                "(\n" +
                "  node[amenity=restaurant](around:" + radiusMeters + "," + lat + "," + lng + ");\n" +
                "  node[tourism=viewpoint](around:"  + radiusMeters + "," + lat + "," + lng + ");\n" +
                "  node[amenity=parking](around:"   + radiusMeters + "," + lat + "," + lng + ");\n" +
                "  node[amenity=fuel](around:"      + radiusMeters + "," + lat + "," + lng + ");\n" +
                ");\n" +
                "out body 40;";   // limit raw results to 40 nodes
    }

    /**
     * Parses the Overpass JSON response into {@link NearbyPoiDTO} objects.
     *
     * Each element carries OSM tags; we derive the category from those tags.
     */
    private List<NearbyPoiDTO> parseOverpassResponse(String json,
                                                     double originLat,
                                                     double originLng) throws Exception {
        JsonNode root     = objectMapper.readTree(json);
        JsonNode elements = root.path("elements");

        List<NearbyPoiDTO> pois = new ArrayList<>();

        for (JsonNode el : elements) {
            double poiLat = el.path("lat").asDouble();
            double poiLng = el.path("lon").asDouble();
            JsonNode tags = el.path("tags");

            String category = resolveCategory(tags);
            if (category == null) continue; // skip unrecognised tags

            String name = tags.path("name").asText("").trim();
            if (name.isEmpty()) name = friendlyLabel(category);

            String[] meta = CATEGORY_META.get(category);
            String   icon = meta[1];

            NearbyPoiDTO poi = new NearbyPoiDTO();
            poi.setPlaceId("osm-" + el.path("id").asText());
            poi.setName(name);
            poi.setCategory(category);
            poi.setCategoryIcon(icon);
            poi.setLatitude(poiLat);
            poi.setLongitude(poiLng);
            poi.setDistanceKm(haversineKm(originLat, originLng, poiLat, poiLng));
            poi.setGoogleMapsUrl(buildMapsUrl(poiLat, poiLng, name));

            pois.add(poi);
        }

        // Sort by distance and cap at MAX_POIS
        return pois.stream()
                .sorted(Comparator.comparingDouble(NearbyPoiDTO::getDistanceKm))
                .limit(MAX_POIS)
                .collect(Collectors.toList());
    }

    /** Maps OSM tags to our internal category key. */
    private String resolveCategory(JsonNode tags) {
        String amenity = tags.path("amenity").asText("");
        String tourism = tags.path("tourism").asText("");

        if ("restaurant".equals(amenity) || "cafe".equals(amenity) || "fast_food".equals(amenity)) {
            return "RESTAURANT";
        }
        if ("viewpoint".equals(tourism)) {
            return "VIEWPOINT";
        }
        if ("parking".equals(amenity)) {
            return "PARKING";
        }
        if ("fuel".equals(amenity)) {
            return "PETROL_STATION";
        }
        return null;
    }

    private String friendlyLabel(String category) {
        return switch (category) {
            case "RESTAURANT"     -> "Restaurant";
            case "VIEWPOINT"      -> "Viewpoint";
            case "PARKING"        -> "Parking";
            case "PETROL_STATION" -> "Petrol Station";
            default               -> category;
        };
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
     * Expects {@code "lat,lng"} e.g. {@code "6.9271,79.8612"}.
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
                } catch (NumberFormatException ignored) {}
            }
        }

        throw new ResourceNotFoundException(
                "Adventure " + adventure.getId() +
                        " does not have valid coordinates. " +
                        "Store the start point as \"lat,lng\" in the location field.");
    }

    /** Google Maps directions deep-link (still works without a key). */
    private String buildMapsUrl(double lat, double lng, String name) {
        return "https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lng
                + "&destination_place_name="
                + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8);
    }

    /** Haversine formula – returns distance in km. */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
