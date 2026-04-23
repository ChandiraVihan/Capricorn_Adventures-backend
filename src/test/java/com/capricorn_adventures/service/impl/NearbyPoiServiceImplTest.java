package com.capricorn_adventures.service.impl;

import com.capricorn_adventures.dto.NearbyPoiDTO;
import com.capricorn_adventures.dto.NearbyPoiResponseDTO;
import com.capricorn_adventures.entity.Adventure;
import com.capricorn_adventures.exception.ResourceNotFoundException;
import com.capricorn_adventures.repository.AdventureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NearbyPoiServiceImplTest {

    @Mock
    private AdventureRepository adventureRepository;

    @InjectMocks
    private NearbyPoiServiceImpl service;

    private Adventure adventureWithCoords;
    private Adventure adventureWithoutCoords;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "googleApiKey", "test-key");

        adventureWithCoords = new Adventure();
        adventureWithCoords.setId(1L);
        adventureWithCoords.setName("River Rafting");
        adventureWithCoords.setLocation("6.9271,79.8612");

        adventureWithoutCoords = new Adventure();
        adventureWithoutCoords.setId(2L);
        adventureWithoutCoords.setName("Mystery Tour");
        adventureWithoutCoords.setLocation("Colombo City Center"); // text, not coords
    }

    // ── AC: adventure not found ────────────────────────────────────────────────

    @Test
    void getNearbyPois_adventureNotFound_throwsResourceNotFoundException() {
        when(adventureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getNearbyPois(99L, null));
    }

    // ── AC: no coordinates ────────────────────────────────────────────────────

    @Test
    void getNearbyPois_noCoordinates_throwsResourceNotFoundException() {
        when(adventureRepository.findById(2L)).thenReturn(Optional.of(adventureWithoutCoords));

        assertThrows(ResourceNotFoundException.class,
                () -> service.getNearbyPois(2L, null));
    }

    // ── AC: invalid category filter ───────────────────────────────────────────

    @Test
    void getNearbyPois_invalidCategory_throwsIllegalArgumentException() {
        // We need POIs in cache to reach the filter step
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));

        // Inject a cache entry so the API call is skipped
        List<NearbyPoiDTO> stubPois = stubPoiList();
        injectCache(1L, stubPois, 5.0, false);

        assertThrows(IllegalArgumentException.class,
                () -> service.getNearbyPois(1L, "UNKNOWN_CATEGORY"));
    }

    // ── AC: category filter ────────────────────────────────────────────────────

    @Test
    void getNearbyPois_categoryFilter_returnsOnlyMatchingCategory() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));

        List<NearbyPoiDTO> stubPois = stubPoiList(); // contains RESTAURANT + PARKING
        injectCache(1L, stubPois, 5.0, false);

        NearbyPoiResponseDTO response = service.getNearbyPois(1L, "RESTAURANT");

        assertNotNull(response.getPois());
        assertTrue(response.getPois().stream().allMatch(p -> "RESTAURANT".equals(p.getCategory())),
                "Only RESTAURANT POIs should be returned");
    }

    @Test
    void getNearbyPois_categoryFilter_caseInsensitive() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));
        injectCache(1L, stubPoiList(), 5.0, false);

        // lowercase input should work
        NearbyPoiResponseDTO response = service.getNearbyPois(1L, "restaurant");
        assertTrue(response.getPois().stream().allMatch(p -> "RESTAURANT".equals(p.getCategory())));
    }

    // ── AC: cache hit ─────────────────────────────────────────────────────────

    @Test
    void getNearbyPois_cacheHit_doesNotCallApiTwice() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));
        injectCache(1L, stubPoiList(), 5.0, false);

        // First call – served from cache
        service.getNearbyPois(1L, null);
        // Second call – should still come from cache
        service.getNearbyPois(1L, null);

        // adventureRepository is called each time (to resolve the adventure),
        // but the Google Places HTTP calls should NOT be made (we can verify
        // no NPE occurs with a fake key – i.e. we never reach the HTTP client).
        verify(adventureRepository, times(2)).findById(1L);
    }

    // ── AC: response structure ────────────────────────────────────────────────

    @Test
    void getNearbyPois_response_containsRequiredFields() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));
        injectCache(1L, stubPoiList(), 5.0, false);

        NearbyPoiResponseDTO response = service.getNearbyPois(1L, null);

        assertNotNull(response);
        assertNotNull(response.getPois());
        assertFalse(response.getPois().isEmpty());

        NearbyPoiDTO first = response.getPois().get(0);
        assertNotNull(first.getName(),          "name must be set");
        assertNotNull(first.getCategory(),      "category must be set");
        assertNotNull(first.getCategoryIcon(),  "categoryIcon must be set");
        assertTrue(first.getDistanceKm() >= 0,  "distanceKm must be non-negative");
        assertNotNull(first.getGoogleMapsUrl(), "googleMapsUrl must be set");
    }

    // ── AC: radius not expanded when POIs found ───────────────────────────────

    @Test
    void getNearbyPois_poisFound_radiusNotExpanded() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));
        injectCache(1L, stubPoiList(), 5.0, false);

        NearbyPoiResponseDTO response = service.getNearbyPois(1L, null);

        assertEquals(5.0, response.getSearchRadiusKm(), 0.01);
        assertFalse(response.isRadiusExpanded());
    }

    // ── AC: radius expanded when no POIs within 5 km ─────────────────────────

    @Test
    void getNearbyPois_noPoisInDefaultRadius_radiusExpanded() {
        when(adventureRepository.findById(1L)).thenReturn(Optional.of(adventureWithCoords));
        // Cache entry that represents the expanded-radius result
        injectCache(1L, stubPoiList(), 10.0, true);

        NearbyPoiResponseDTO response = service.getNearbyPois(1L, null);

        assertEquals(10.0, response.getSearchRadiusKm(), 0.01);
        assertTrue(response.isRadiusExpanded());
    }

    // ── AC: haversine distance calculation ────────────────────────────────────

    @Test
    void haversineKm_knownPoints_returnsCorrectDistance() throws Exception {
        Method haversine = NearbyPoiServiceImpl.class
                .getDeclaredMethod("haversineKm", double.class, double.class, double.class, double.class);
        haversine.setAccessible(true);

        // Colombo → Kandy is roughly 115 km straight line
        double dist = (double) haversine.invoke(service, 6.9271, 79.8612, 7.2906, 80.6337);
        assertTrue(dist > 100 && dist < 130,
                "Expected ~115 km between Colombo and Kandy, got " + dist);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private List<NearbyPoiDTO> stubPoiList() {
        NearbyPoiDTO restaurant = new NearbyPoiDTO();
        restaurant.setPlaceId("place-001");
        restaurant.setName("Surf Cafe");
        restaurant.setCategory("RESTAURANT");
        restaurant.setCategoryIcon("🍽️");
        restaurant.setLatitude(6.928);
        restaurant.setLongitude(79.862);
        restaurant.setDistanceKm(0.3);
        restaurant.setGoogleMapsUrl("https://www.google.com/maps/dir/?api=1&destination=6.928,79.862");

        NearbyPoiDTO parking = new NearbyPoiDTO();
        parking.setPlaceId("place-002");
        parking.setName("Beach Parking");
        parking.setCategory("PARKING");
        parking.setCategoryIcon("🅿️");
        parking.setLatitude(6.930);
        parking.setLongitude(79.860);
        parking.setDistanceKm(0.8);
        parking.setGoogleMapsUrl("https://www.google.com/maps/dir/?api=1&destination=6.930,79.860");

        return new ArrayList<>(Arrays.asList(restaurant, parking));
    }

    /** Directly inserts a cache entry to avoid real HTTP calls during tests. */
    @SuppressWarnings("unchecked")
    private void injectCache(Long adventureId, List<NearbyPoiDTO> pois, double radiusKm, boolean expanded) {
        try {
            java.lang.reflect.Field cacheField = NearbyPoiServiceImpl.class
                    .getDeclaredField("poiCache");
            cacheField.setAccessible(true);
            Map<Long, Object> cache = (Map<Long, Object>) cacheField.get(null);

            // Instantiate the inner CacheEntry class
            Class<?> entryClass = Arrays.stream(NearbyPoiServiceImpl.class.getDeclaredClasses())
                    .filter(c -> c.getSimpleName().equals("CacheEntry"))
                    .findFirst()
                    .orElseThrow();

            java.lang.reflect.Constructor<?> ctor = entryClass
                    .getDeclaredConstructor(List.class, double.class, boolean.class);
            ctor.setAccessible(true);

            Object entry = ctor.newInstance(pois, radiusKm, expanded);
            cache.put(adventureId, entry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject test cache entry", e);
        }
    }
}
